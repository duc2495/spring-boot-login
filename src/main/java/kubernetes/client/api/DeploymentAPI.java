package kubernetes.client.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import kubernetes.client.model.Application;
import kubernetes.client.model.ProactiveAutoscaler;
import kubernetes.client.model.ResourcesRequest;
import kubernetes.client.model.Template;
import kubernetes.client.model.Volume;

@Repository
public class DeploymentAPI extends ConnectK8SConfiguration {

	public void create(Application app, String namespace) {
		try {
			// Create a deployment
			Deployment deployment = new DeploymentBuilder().withNewMetadata().withName(app.getName())
					.addToLabels("app", app.getName()).endMetadata().withNewSpec().withReplicas(app.getPods())
					.withNewSelector().addToMatchLabels("app", app.getName()).endSelector().withNewTemplate()
					.withNewMetadata().addToLabels("app", app.getName()).endMetadata().withNewSpec().addNewContainer()
					.withName(app.getName()).withImage(app.getImage()).addNewPort().withContainerPort(app.getPort())
					.endPort().withNewResources().addToRequests("cpu", new Quantity("500m"))
					.addToRequests("memory", new Quantity("1Gi")).endResources().endContainer().endSpec().endTemplate()
					.endSpec().build();
			logger.info("{}: {}", "Created deployment",
					client.extensions().deployments().inNamespace(namespace).create(deployment));

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Throwable[] suppressed = e.getSuppressed();
			if (suppressed != null) {
				for (Throwable t : suppressed) {
					logger.error(t.getMessage(), t);
				}
			}
		}

	}

	public void create(Template template, String namespace) {
		try {
			// Create a deployment
			Deployment deployment = new DeploymentBuilder().withNewMetadata().withName(template.getName())
					.addToLabels("app", template.getName()).endMetadata().withNewSpec().withReplicas(1)
					.withNewSelector().addToMatchLabels("app", template.getName()).endSelector().withNewTemplate()
					.withNewMetadata().addToLabels("app", template.getName()).endMetadata().withNewSpec()
					.addNewContainer().withName(template.getName())
					.withImage(template.getImage() + ":" + template.getTag()).withEnv(template.getEnvs()).addNewPort()
					.withContainerPort(template.getPort()).endPort().withNewResources()
					.addToRequests("cpu", new Quantity("100m")).addToRequests("memory", new Quantity("300Mi"))
					.endResources().addNewVolumeMount().withName(template.getName())
					.withMountPath(template.getMountPath()).withReadOnly(false).endVolumeMount().endContainer()
					.addNewVolume().withName(template.getName()).withNewPersistentVolumeClaim()
					.withClaimName(template.getName()).endPersistentVolumeClaim().and().endSpec().endTemplate()
					.endSpec().build();
			logger.info("{}: {}", "Created deployment",
					client.extensions().deployments().inNamespace(namespace).create(deployment));

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Throwable[] suppressed = e.getSuppressed();
			if (suppressed != null) {
				for (Throwable t : suppressed) {
					logger.error(t.getMessage(), t);
				}
			}
		}
	}

	public void createAutoscaler(ProactiveAutoscaler pa, String appName, String namespace) {
		try {
			// Create a deployment
			String image = "duc2495/tf-serving-client:latest";
			List<EnvVar> envs = new ArrayList<EnvVar>();
			envs.add(new EnvVar("TF_SERVER", "tensorflow-serving:9000", null));

			envs.add(new EnvVar("INFLUX_SERVER", "monitoring-influxdb.kube-system:8086", null));
			envs.add(new EnvVar("WEB_SERVER", "dashboard:8080", null));
			envs.add(new EnvVar("NAMESPACE", namespace, null));
			envs.add(new EnvVar("NAME", appName, null));
			envs.add(new EnvVar("MODEL_NAME", "cpu", null));
			String command = "bin/sh";
			List<String> args = new ArrayList<String>();
			args.add("-c");
			args.add("while true ; do ./run_client.sh & sleep 60; done");
			Deployment deployment = new DeploymentBuilder().withNewMetadata().withName(pa.getName())
					.addToLabels("app", pa.getName()).endMetadata().withNewSpec().withNewTemplate().withNewMetadata()
					.addToLabels("app", pa.getName()).endMetadata().withNewSpec().addNewContainer()
					.withName(pa.getName()).withImage(image).withEnv(envs).withCommand(command).withArgs(args)
					.endContainer().endSpec().endTemplate().endSpec().build();
			logger.info("{}: {}", "Created autoscaler job",
					client.extensions().deployments().inNamespace("default").create(deployment));

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Throwable[] suppressed = e.getSuppressed();
			if (suppressed != null) {
				for (Throwable t : suppressed) {
					logger.error(t.getMessage(), t);
				}
			}
		}

	}

	public Deployment get(String name, String namespace) {
		try {
			// Get a deployment
			Deployment deployment = client.extensions().deployments().inNamespace(namespace).withName(name).get();
			logger.info("{}: {}", "Get deployment", deployment);
			return deployment;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Throwable[] suppressed = e.getSuppressed();
			if (suppressed != null) {
				for (Throwable t : suppressed) {
					logger.error(t.getMessage(), t);
				}
			}
		}
		return null;
	}

	public List<Deployment> getAll(String namespace) {
		try {
			// Get all deployment
			List<Deployment> deployments = client.extensions().deployments().inNamespace(namespace).list().getItems();
			return deployments;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Throwable[] suppressed = e.getSuppressed();
			if (suppressed != null) {
				for (Throwable t : suppressed) {
					logger.error(t.getMessage(), t);
				}
			}
		}
		return null;
	}

	public void delete(String name, String namespace) {
		try {
			// Delete a deployment
			logger.info("{}: {}", "Delete deployment",
					client.extensions().deployments().inNamespace(namespace).withName(name).delete());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Throwable[] suppressed = e.getSuppressed();
			if (suppressed != null) {
				for (Throwable t : suppressed) {
					logger.error(t.getMessage(), t);
				}
			}
		}
	}

	public void update(Application app, String namespace) {
		try {
			// Update a deployment
			logger.info("{}: {}", "Update deployment",
					client.extensions().deployments().inNamespace(namespace).withName(app.getName()).edit().editSpec()
							.editTemplate().editOrNewSpec().editFirstContainer().withName(app.getName())
							.withImage(app.getImage()).editFirstPort().withContainerPort(app.getPort()).endPort()
							.endContainer().endSpec().endTemplate().endSpec().done());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Throwable[] suppressed = e.getSuppressed();
			if (suppressed != null) {
				for (Throwable t : suppressed) {
					logger.error(t.getMessage(), t);
				}
			}
		}
	}

	public void addStorage(Deployment deploy, Volume volume) {
		try {
			// Add a Storage

			logger.info("{}: {}", "Add a Storage",
					client.extensions().deployments().inNamespace(deploy.getMetadata().getNamespace())
							.withName(deploy.getMetadata().getName()).edit().editSpec().editTemplate().editSpec()
							.editFirstContainer().addNewVolumeMount().withName(volume.getStorageName())
							.withMountPath(volume.getMountPath()).withReadOnly(false).endVolumeMount().endContainer()
							.addNewVolume().withName(volume.getStorageName()).withNewPersistentVolumeClaim()
							.withClaimName(volume.getStorageName()).endPersistentVolumeClaim().endVolume().endSpec()
							.endTemplate().endSpec().done());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Throwable[] suppressed = e.getSuppressed();
			if (suppressed != null) {
				for (Throwable t : suppressed) {
					logger.error(t.getMessage(), t);
				}
			}
		}
	}

	public void deleteStorage(Deployment deploy) {
		try {
			// Unmount a Storage
			deploy.getSpec().getTemplate().getSpec().getVolumes()
					.removeAll(deploy.getSpec().getTemplate().getSpec().getVolumes());
			deploy.getSpec().getTemplate().getSpec().getContainers().get(0).getVolumeMounts()
					.removeAll(deploy.getSpec().getTemplate().getSpec().getContainers().get(0).getVolumeMounts());
			logger.info("{}: {}", "Delete a Storage",
					client.extensions().deployments().inNamespace(deploy.getMetadata().getNamespace())
							.withName(deploy.getMetadata().getName()).replace(deploy));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Throwable[] suppressed = e.getSuppressed();
			if (suppressed != null) {
				for (Throwable t : suppressed) {
					logger.error(t.getMessage(), t);
				}
			}
		}
	}

	public void scale(Deployment deploy) {
		try {
			// Scale a deployment
			logger.info("{}: {}", "Scale deployment",
					client.extensions().deployments().inNamespace(deploy.getMetadata().getNamespace())
							.withName(deploy.getMetadata().getName()).scale(deploy.getSpec().getReplicas(), true));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Throwable[] suppressed = e.getSuppressed();
			if (suppressed != null) {
				for (Throwable t : suppressed) {
					logger.error(t.getMessage(), t);
				}
			}
		}
	}

	public void rollBack(Deployment deployment, Long revision) {
		try {
			// Update a deployment
			logger.info("{}: {}", "Roll back deployment",
					client.extensions().deployments().inNamespace(deployment.getMetadata().getNamespace())
							.withName(deployment.getMetadata().getName()).edit().editSpec().editOrNewRollbackTo()
							.withRevision(revision).endRollbackTo().endSpec().done());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Throwable[] suppressed = e.getSuppressed();
			if (suppressed != null) {
				for (Throwable t : suppressed) {
					logger.error(t.getMessage(), t);
				}
			}
		}
	}
	
	public void editResources(Deployment deployment, ResourcesRequest resources) {
		try {
			// Edit Resousces 
			Map <String, Quantity> mapResources = new HashMap<String, Quantity>();
			mapResources.put("cpu", new Quantity(resources.getCpu()));
			mapResources.put("memory", new Quantity(resources.getMemory()));
			logger.info("{}: {}", "Edit resources",
					client.extensions().deployments().inNamespace(deployment.getMetadata().getNamespace()).withName(deployment.getMetadata().getName()).edit().editSpec()
							.editTemplate().editOrNewSpec().editFirstContainer().editOrNewResources().addToRequests(mapResources).endResources()
							.endContainer().endSpec().endTemplate().endSpec().done());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Throwable[] suppressed = e.getSuppressed();
			if (suppressed != null) {
				for (Throwable t : suppressed) {
					logger.error(t.getMessage(), t);
				}
			}
		}
	}

	public void pause(Deployment deployment) {
		try {
			// Pause a deployment
			logger.info("{}: {}", "Pause deployment",
					client.extensions().deployments().inNamespace(deployment.getMetadata().getNamespace())
							.withName(deployment.getMetadata().getName()).edit().editSpec().withPaused(true).and()
							.done());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Throwable[] suppressed = e.getSuppressed();
			if (suppressed != null) {
				for (Throwable t : suppressed) {
					logger.error(t.getMessage(), t);
				}
			}
		}
	}

	public boolean exists(String name, String namespace) {
		try {
			if (client.extensions().deployments().inNamespace(namespace).withName(name).get() != null) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Throwable[] suppressed = e.getSuppressed();
			if (suppressed != null) {
				for (Throwable t : suppressed) {
					logger.error(t.getMessage(), t);
				}
			}
		}
		return false;
	}

}
