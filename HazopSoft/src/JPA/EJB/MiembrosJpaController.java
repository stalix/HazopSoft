/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package JPA.EJB;

import JPA.EJB.exceptions.NonexistentEntityException;
import JPA.EJB.exceptions.PreexistingEntityException;
import JPA.Miembros;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author Alvaro Monsalve
 */
public class MiembrosJpaController implements Serializable {

    public MiembrosJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Miembros miembros) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(miembros);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMiembros(miembros.getId()) != null) {
                throw new PreexistingEntityException("Miembros " + miembros + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Miembros miembros) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            miembros = em.merge(miembros);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = miembros.getId();
                if (findMiembros(id) == null) {
                    throw new NonexistentEntityException("The miembros with id " + id + " no longer exists.");
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
            Miembros miembros;
            try {
                miembros = em.getReference(Miembros.class, id);
                miembros.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The miembros with id " + id + " no longer exists.", enfe);
            }
            em.remove(miembros);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Miembros> findMiembrosEntities() {
        return findMiembrosEntities(true, -1, -1);
    }

    public List<Miembros> findMiembrosEntities(int maxResults, int firstResult) {
        return findMiembrosEntities(false, maxResults, firstResult);
    }

    private List<Miembros> findMiembrosEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Miembros.class));
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

    public Miembros findMiembros(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Miembros.class, id);
        } finally {
            em.close();
        }
    }

    public int getMiembrosCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Miembros> rt = cq.from(Miembros.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
