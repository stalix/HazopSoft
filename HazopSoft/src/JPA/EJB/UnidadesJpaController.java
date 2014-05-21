/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package JPA.EJB;

import JPA.EJB.exceptions.NonexistentEntityException;
import JPA.EJB.exceptions.PreexistingEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import JPA.Proyectos;
import JPA.Unidades;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Alvaro Monsalve
 */
public class UnidadesJpaController implements Serializable {

    public UnidadesJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Unidades unidades) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Proyectos idProyectos = unidades.getIdProyectos();
            if (idProyectos != null) {
                idProyectos = em.getReference(idProyectos.getClass(), idProyectos.getId());
                unidades.setIdProyectos(idProyectos);
            }
            em.persist(unidades);
            if (idProyectos != null) {
                idProyectos.getUnidadesList().add(unidades);
                idProyectos = em.merge(idProyectos);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findUnidades(unidades.getId()) != null) {
                throw new PreexistingEntityException("Unidades " + unidades + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Unidades unidades) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Unidades persistentUnidades = em.find(Unidades.class, unidades.getId());
            Proyectos idProyectosOld = persistentUnidades.getIdProyectos();
            Proyectos idProyectosNew = unidades.getIdProyectos();
            if (idProyectosNew != null) {
                idProyectosNew = em.getReference(idProyectosNew.getClass(), idProyectosNew.getId());
                unidades.setIdProyectos(idProyectosNew);
            }
            unidades = em.merge(unidades);
            if (idProyectosOld != null && !idProyectosOld.equals(idProyectosNew)) {
                idProyectosOld.getUnidadesList().remove(unidades);
                idProyectosOld = em.merge(idProyectosOld);
            }
            if (idProyectosNew != null && !idProyectosNew.equals(idProyectosOld)) {
                idProyectosNew.getUnidadesList().add(unidades);
                idProyectosNew = em.merge(idProyectosNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = unidades.getId();
                if (findUnidades(id) == null) {
                    throw new NonexistentEntityException("The unidades with id " + id + " no longer exists.");
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
            Unidades unidades;
            try {
                unidades = em.getReference(Unidades.class, id);
                unidades.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The unidades with id " + id + " no longer exists.", enfe);
            }
            Proyectos idProyectos = unidades.getIdProyectos();
            if (idProyectos != null) {
                idProyectos.getUnidadesList().remove(unidades);
                idProyectos = em.merge(idProyectos);
            }
            em.remove(unidades);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Unidades> findUnidadesEntities() {
        return findUnidadesEntities(true, -1, -1);
    }

    public List<Unidades> findUnidadesEntities(int maxResults, int firstResult) {
        return findUnidadesEntities(false, maxResults, firstResult);
    }

    private List<Unidades> findUnidadesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Unidades.class));
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

    public Unidades findUnidades(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Unidades.class, id);
        } finally {
            em.close();
        }
    }

    public int getUnidadesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Unidades> rt = cq.from(Unidades.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
