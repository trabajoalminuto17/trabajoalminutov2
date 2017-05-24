/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.basp.trabajo_al_minuto.web.view;

import static com.basp.trabajo_al_minuto.web.model.AtributosWeb.DETALLE_PRUEBA_PLANTILLA_PAGE;

import com.basp.trabajo_al_minuto.model.business.BusinessException;
import com.basp.trabajo_al_minuto.service.entity.Citacion;
import com.basp.trabajo_al_minuto.service.entity.Evaluacion;
import com.basp.trabajo_al_minuto.service.entity.PerfilHasPrueba;
import com.basp.trabajo_al_minuto.service.entity.PruebaPlantilla;
import com.basp.trabajo_al_minuto.service.entity.Respuesta;
import com.basp.trabajo_al_minuto.service.entity.Usuario;
import static com.basp.trabajo_al_minuto.web.model.AtributosWeb.EVALUACION_PAGE;
import static com.basp.trabajo_al_minuto.web.model.AtributosWeb.PORTAL_PAGE;
import com.basp.trabajo_al_minuto.web.model.ComponenteWeb;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.evaluarPrueba;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.formatDateTime;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;

/**
 *
 * @author BASP
 */
@Named
@ViewScoped
public class VerPruebaView extends ComponenteWeb implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<PruebaPlantilla> filteredPruebasPlantilla;
    private List<PerfilHasPrueba> filteredPruebas;
    private PruebaPlantilla pruebaPlantillaSeleccionada;
    private PerfilHasPrueba pruebaSeleccionada;
    private List<PerfilHasPrueba> pruebasByPerfil;
    private List<PruebaPlantilla> pruebasByEmpresa;
    private Usuario usuariologin;
    private Boolean pruebasCompletas;

    @PostConstruct
    public void init() {
        try {
            usuariologin = getUserLogin();
            pruebasCompletas = Boolean.FALSE;
            if (usuariologin.getRol().getRolId() == 3L) {
                if (getPruebasPendientes() != null && getEvaluacionId() != null) {
                    pruebasByPerfil = getPruebasPendientes();
                    if (!getPruebasResueltas().isEmpty()) {
                        Evaluacion e = pruebaEjb.findEvaluacion(getEvaluacionId());
                        Double porcentaje = 0D;
                        List<Respuesta> respuestas = citacionEjb.getRespuestasByEvaluacionPrueba(e.getEvaluacionId(), getPruebaPerfil().getPrueba().getPruebaId());
                        porcentaje = porcentaje + evaluarPrueba(respuestas, getPruebaPerfil());
                        e.setFechaFin(new Date());
                        e.setTiempoEmpleado(tiempoUsado(e.getTiempoEmpleado()));
                        e.setPorcentaje(e.getPorcentaje() + porcentaje);
                        pruebaEjb.updateEvaluacion(e);
                        pruebasByPerfil.removeAll(getPruebasResueltas());
                    }
                    if (pruebasByPerfil.isEmpty()) {
                        Evaluacion e = pruebaEjb.findEvaluacion(getEvaluacionId());
                        e.setTiempoEmpleado(tiempoUsado(getInicioEvaluacion()));
                        e.setFechaFin(new Date());
                        pruebaEjb.updateEvaluacion(e);
                        usuariologin.getCandidato().setPruebasActivas(Boolean.FALSE);
                        usuariologin = usuarioEjb.updateUsuario(usuariologin);
                        pruebasCompletas = Boolean.TRUE;
                        Citacion c = citacionEjb.findCitacion(getCitacionId());
                        c.setActivarPruebas(Boolean.FALSE);
                        c.setResuelto(Boolean.TRUE);
                        citacionEjb.updateCitacion(c);
                    }
                }
            } else {
                pruebasByEmpresa = pruebaEjb.getPruebasPlantillaByEmpresa(usuariologin.getEmpresa().getEmpresaId());
            }
        } catch (BusinessException ex) {
            Logger.getLogger(EvaluacionView.class.getName()).log(Level.SEVERE, ex.developerException());
        }
    }

    public void onRowSelectVerPruebas(SelectEvent event) {
        try {
            if (usuariologin.getRol().getRolId() == 3L) {
                pruebaSeleccionada = (PerfilHasPrueba) event.getObject();
            } else {
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("pruebaPlantillaId", ((PruebaPlantilla) event.getObject()).getPruebaId());
                FacesContext.getCurrentInstance().getExternalContext().redirect(DETALLE_PRUEBA_PLANTILLA_PAGE);
            }
        } catch (IOException ex) {
            Logger.getLogger(VerPruebaView.class.getName()).log(Level.SEVERE, "onRowSelectVerOfertas", ex);
        }
    }

    public void comenzarPrueba() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("pruebaperfil", pruebaSeleccionada);
            FacesContext.getCurrentInstance().getExternalContext().redirect(EVALUACION_PAGE);
        } catch (IOException ex) {
            Logger.getLogger(VerPruebaView.class.getName()).log(Level.SEVERE, "comenzarPrueba", ex);
        }
    }

    public void finalizarPrueba() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            FacesContext.getCurrentInstance().getExternalContext().redirect(PORTAL_PAGE);
        } catch (IOException ex) {
            Logger.getLogger(VerPruebaView.class.getName()).log(Level.SEVERE, "finalizarPrueba", ex);
        }
    }

    public String getFormatDate(Date date) {
        if (date != null) {
            return formatDateTime(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
        }
        return "";
    }

    private Date tiempoUsado(Date d) {
        LocalTime a = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()).toLocalTime();
        LocalTime b = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()).toLocalTime();
        LocalDateTime ldt = LocalDateTime.of(LocalDate.now(), LocalTime.ofSecondOfDay(ChronoUnit.SECONDS.between(a, b)));
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

//    @Getter and Setter
    public List<PruebaPlantilla> getFilteredPruebasPlantilla() {
        return filteredPruebasPlantilla;
    }

    public void setFilteredPruebasPlantilla(List<PruebaPlantilla> filteredPruebasPlantilla) {
        this.filteredPruebasPlantilla = filteredPruebasPlantilla;
    }

    public List<PerfilHasPrueba> getFilteredPruebas() {
        return filteredPruebas;
    }

    public void setFilteredPruebas(List<PerfilHasPrueba> filteredPruebas) {
        this.filteredPruebas = filteredPruebas;
    }

    public PruebaPlantilla getPruebaPlantillaSeleccionada() {
        return pruebaPlantillaSeleccionada;
    }

    public void setPruebaPlantillaSeleccionada(PruebaPlantilla pruebaPlantillaSeleccionada) {
        this.pruebaPlantillaSeleccionada = pruebaPlantillaSeleccionada;
    }

    public PerfilHasPrueba getPruebaSeleccionada() {
        return pruebaSeleccionada;
    }

    public void setPruebaSeleccionada(PerfilHasPrueba pruebaSeleccionada) {
        this.pruebaSeleccionada = pruebaSeleccionada;
    }

    public List<PerfilHasPrueba> getPruebasByPerfil() {
        return pruebasByPerfil;
    }

    public void setPruebasByPerfil(List<PerfilHasPrueba> pruebasByPerfil) {
        this.pruebasByPerfil = pruebasByPerfil;
    }

    public List<PruebaPlantilla> getPruebasByEmpresa() {
        return pruebasByEmpresa;
    }

    public void setPruebasByEmpresa(List<PruebaPlantilla> pruebasByEmpresa) {
        this.pruebasByEmpresa = pruebasByEmpresa;
    }

    public Usuario getUsuariologin() {
        return usuariologin;
    }

    public void setUsuariologin(Usuario usuariologin) {
        this.usuariologin = usuariologin;
    }

    public Boolean getPruebasCompletas() {
        return pruebasCompletas;
    }

    public void setPruebasCompletas(Boolean pruebasCompletas) {
        this.pruebasCompletas = pruebasCompletas;
    }

}
