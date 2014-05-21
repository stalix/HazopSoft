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
import JPA.Empresas;
import JPA.Proyectos;
import JPA.Unidades;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Alvaro Monsalve
 */
public class ProyectosJpaController implements Serializable {

    public ProyectosJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Proyectos proyectos) throws PreexistingEntityException, Exception {
        if (proyectos.getUnidadesList() == null) {
            proyectos.setUnidadesList(new ArrayList<Unidades>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Empresas idEmpresa = proyectos.getIdEmpresa();
            if (idEmpresa != null) {
                idEmpresa = em.getReference(idEmpresa.getClass(), idEmpresa.getId());
                proyectos.setIdEmpresa(idEmpresa);
            }
            List<Unidades> attachedUnidadesList = new ArrayList<Unidades>();
            for (Unidades unidadesListUnidadesToAttach : proyectos.getUnidadesList()) {
                unidadesListUnidadesToAttach = em.getReference(unidadesListUnidadesToAttach.getClass(), unidadesListUnidadesToAttach.getId());
                attachedUnidadesList.add(unidadesListUnidadesToAttach);
            }
            proyectos.setUnidadesList(attachedUnidadesList);
            em.persist(proyectos);
            if (idEmpresa != null) {
                idEmpresa.getProyectosList().add(proyectos);
                idEmpresa = em.merge(idEmpresa);
            }
            for (Unidades unidadesListUnidades : proyectos.getUnidadesList()) {
                Proyectos oldIdProyectosOfUnidadesListUnidades = unidadesListUnidades.getIdProyectos();
                unidadesListUnidades.setIdProyectos(proyectos);
                unidadesListUnidades = em.merge(unidadesListUnidades);
                if (oldIdProyectosOfUnidadesListUnidades != null) {
                    oldIdProyectosOfUnidadesListUnidades.getUnidadesList().remove(unidadesListUnidades);
                    oldIdProyectosOfUnidadesListUnidades = em.merge(oldIdProyectosOfUnidadesListUnidades);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findProyectos(proyectos.getId()) != null) {
                throw new PreexistingEntityException("Proyectos " + proyectos + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Proyectos proyectos) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Proyectos persistentProyectos = em.find(Proyectos.class, proyectos.getId());
            Empresas idEmpresaOld = persistentProyectos.getIdEmpresa();
            Empresas idEmpresaNew = proyectos.getIdEmpresa();
            List<Unidades> unidadesListOld = persistentProyectos.getUnidadesList();
            List<Unidades> unidadesListNew = proyectos.getUnidadesList();
            if (idEmpresaNew != null) {
                idEmpresaNew = em.getReference(idEmpresaNew.getClass(), idEmpresaNew.getId());
                proyectos.setIdEmpresa(idEmpresaNew);
            }
            List<Unidades> attachedUnidadesListNew = new ArrayList<Unidades>();
            for (Unidades unidadesListNewUnidadesToAttach : unidadesListNew) {
                unidadesListNewUnidadesToAttach = em.getReference(unidadesListNewUnidadesToAttach.getClass(), unidadesListNewUnidadesToAttach.getId());
                attachedUnidadesListNew.add(unidadesListNewUnidadesToAttach);
            }
            unidadesListNew = attachedUnidadesListNew;
            proyectos.setUnidadesList(unidadesListNew);
            proyectos = em.merge(proyectos);
            if (idEmpresaOld != null && !idEmpresaOld.equals(idEmpresaNew)) {
                idEmpresaOld.getProyectosList().remove(proyectos);
                idEmpresaOld = em.merge(idEmpresaOld);
            }
            if (idEmpresaNew != null && !idEmpresaNew.equals(idEmpresaOld)) {
                idEmpresaNew.getProyectosList().add(proyectos);
                idEmpresaNew = em.merge(idEmpresaNew);
            }
            for (Unidades unidadesListOldUnidades : unidadesListOld) {
                if (!unidadesListNew.contains(unidadesListOldUnidades)) {
                    unidadesListOldUnidades.setIdProyectos(null);
                    unidadesListOldUnidades = em.merge(unidadesListOldUnidades);
                }
            }
            for (Unidades unidadesListNewUnidades : unidadesListNew) {
                if (!unidadesListOld.contains(unidadesListNewUnidades)) {
                    Proyectos oldIdProyectosOfUnidadesListNewUnidades = unidadesListNewUnidades.getIdProyectos();
                    unidadesListNewUnidades.setIdProyectos(proyectos);
                    unidadesListNewUnidades = em.merge(unidadesListNewUnidades);
                    if (oldIdProyectosOfUnidadesListNewUnidades != null && !oldIdProyectosOfUnidadesListNewUnidades.equals(proyectos)) {
                        oldIdProyectosOfUnidadesListNewUnidades.getUnidadesList().remove(unidadesListNewUnidades);
                        oldIdProyectosOfUnidadesListNewUnidades = em.merge(oldIdProyectosOfUnidadesListNewUnidades);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = proyectos.getId();
                if (findProyectos(id) == null) {
                    throw new NonexistentEntityException("The proyectos with id " + id + " no longer exists.");
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
            Proyectos proyectos;
            try {
                proyectos = em.getReference(Proyectos.class, id);
                proyectos.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The proyectos with id " + id + " no longer exists.", enfe);
            }
            Empresas idEmpresa = proyectos.getIdEmpresa();
            if (idEmpresa != null) {
                idEmpresa.getProyectosList().remove(proyectos);
                idEmpresa = em.merge(idEmpresa);
            }
            List<Unidades> unidadesList = proyectos.getUnidadesList();
            for (Unidades unidadesListUnidades : unidadesList) {
                unidadesListUnidades.setIdProyectos(null);
                unidadesListUnidades = em.merge(unidadesListUnidades);
            }
            em.remove(proyectos);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Proyectos> findProyectosEntities() {
        return findProyectosEntities(true, -1, -1);
    }

    public List<Proyectos> findProyectosEntities(int maxResults, int firstResult) {
        return findProyectosEntities(false, maxResults, firstResult);
    }

    private List<Proyectos> findProyectosEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Proyectos.class));
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

    public Proyectos findProyectos(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Proyectos.class, id);
        } finally {
            em.close();
        }
    }

    public int getProyectosCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Proyectos> rt = cq.from(Proyectos.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
