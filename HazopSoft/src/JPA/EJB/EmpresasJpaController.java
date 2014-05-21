/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package JPA.EJB;

import JPA.EJB.exceptions.NonexistentEntityException;
import JPA.EJB.exceptions.PreexistingEntityException;
import JPA.Empresas;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import JPA.Proyectos;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Alvaro Monsalve
 */
public class EmpresasJpaController implements Serializable {

    public EmpresasJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Empresas empresas) throws PreexistingEntityException, Exception {
        if (empresas.getProyectosList() == null) {
            empresas.setProyectosList(new ArrayList<Proyectos>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Proyectos> attachedProyectosList = new ArrayList<Proyectos>();
            for (Proyectos proyectosListProyectosToAttach : empresas.getProyectosList()) {
                proyectosListProyectosToAttach = em.getReference(proyectosListProyectosToAttach.getClass(), proyectosListProyectosToAttach.getId());
                attachedProyectosList.add(proyectosListProyectosToAttach);
            }
            empresas.setProyectosList(attachedProyectosList);
            em.persist(empresas);
            for (Proyectos proyectosListProyectos : empresas.getProyectosList()) {
                Empresas oldIdEmpresaOfProyectosListProyectos = proyectosListProyectos.getIdEmpresa();
                proyectosListProyectos.setIdEmpresa(empresas);
                proyectosListProyectos = em.merge(proyectosListProyectos);
                if (oldIdEmpresaOfProyectosListProyectos != null) {
                    oldIdEmpresaOfProyectosListProyectos.getProyectosList().remove(proyectosListProyectos);
                    oldIdEmpresaOfProyectosListProyectos = em.merge(oldIdEmpresaOfProyectosListProyectos);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findEmpresas(empresas.getId()) != null) {
                throw new PreexistingEntityException("Empresas " + empresas + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Empresas empresas) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Empresas persistentEmpresas = em.find(Empresas.class, empresas.getId());
            List<Proyectos> proyectosListOld = persistentEmpresas.getProyectosList();
            List<Proyectos> proyectosListNew = empresas.getProyectosList();
            List<Proyectos> attachedProyectosListNew = new ArrayList<Proyectos>();
            for (Proyectos proyectosListNewProyectosToAttach : proyectosListNew) {
                proyectosListNewProyectosToAttach = em.getReference(proyectosListNewProyectosToAttach.getClass(), proyectosListNewProyectosToAttach.getId());
                attachedProyectosListNew.add(proyectosListNewProyectosToAttach);
            }
            proyectosListNew = attachedProyectosListNew;
            empresas.setProyectosList(proyectosListNew);
            empresas = em.merge(empresas);
            for (Proyectos proyectosListOldProyectos : proyectosListOld) {
                if (!proyectosListNew.contains(proyectosListOldProyectos)) {
                    proyectosListOldProyectos.setIdEmpresa(null);
                    proyectosListOldProyectos = em.merge(proyectosListOldProyectos);
                }
            }
            for (Proyectos proyectosListNewProyectos : proyectosListNew) {
                if (!proyectosListOld.contains(proyectosListNewProyectos)) {
                    Empresas oldIdEmpresaOfProyectosListNewProyectos = proyectosListNewProyectos.getIdEmpresa();
                    proyectosListNewProyectos.setIdEmpresa(empresas);
                    proyectosListNewProyectos = em.merge(proyectosListNewProyectos);
                    if (oldIdEmpresaOfProyectosListNewProyectos != null && !oldIdEmpresaOfProyectosListNewProyectos.equals(empresas)) {
                        oldIdEmpresaOfProyectosListNewProyectos.getProyectosList().remove(proyectosListNewProyectos);
                        oldIdEmpresaOfProyectosListNewProyectos = em.merge(oldIdEmpresaOfProyectosListNewProyectos);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = empresas.getId();
                if (findEmpresas(id) == null) {
                    throw new NonexistentEntityException("The empresas with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Empresas empresas;
            try {
                empresas = em.getReference(Empresas.class, id);
                empresas.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The empresas with id " + id + " no longer exists.", enfe);
            }
            List<Proyectos> proyectosList = empresas.getProyectosList();
            for (Proyectos proyectosListProyectos : proyectosList) {
                proyectosListProyectos.setIdEmpresa(null);
                proyectosListProyectos = em.merge(proyectosListProyectos);
            }
            em.remove(empresas);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Empresas> findEmpresasEntities() {
        return findEmpresasEntities(true, -1, -1);
    }

    public List<Empresas> findEmpresasEntities(int maxResults, int firstResult) {
        return findEmpresasEntities(false, maxResults, firstResult);
    }

    private List<Empresas> findEmpresasEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Empresas.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Empresas findEmpresas(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Empresas.class, id);
        } finally {
            em.close();
        }
    }

    public int getEmpresasCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Empresas> rt = cq.from(Empresas.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
