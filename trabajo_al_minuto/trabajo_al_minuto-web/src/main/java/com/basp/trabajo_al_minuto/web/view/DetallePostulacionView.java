/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.basp.trabajo_al_minuto.web.view;

import static com.basp.trabajo_al_minuto.model.business.BusinessAttributes.HOST_EMAIL_SERVER;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.CHANGE_NOT;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.CHANGE_OK;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.CITACION_NOT;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.CITACION_OK;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.CITACION_RECHAZADA_NOT;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.CITACION_RECHAZADA_OK;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.descargarArchivoPdf;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.webMessage;

import com.basp.trabajo_al_minuto.model.business.BusinessException;
import com.basp.trabajo_al_minuto.model.business.BusinessHtmlTemplates;
import static com.basp.trabajo_al_minuto.model.business.BusinessUtils.sendEmail;
import com.basp.trabajo_al_minuto.model.dto.EmailMessage;
import com.basp.trabajo_al_minuto.service.entity.Catalogo;
import com.basp.trabajo_al_minuto.service.entity.Citacion;
import com.basp.trabajo_al_minuto.service.entity.Oferta;
import com.basp.trabajo_al_minuto.service.entity.PerfilHasPrueba;
import com.basp.trabajo_al_minuto.service.entity.Usuario;
import com.basp.trabajo_al_minuto.service.entity.UsuarioHasOferta;
import static com.basp.trabajo_al_minuto.web.model.AtributosWeb.DETALLE_EVALUACION_PAGE;
import com.basp.trabajo_al_minuto.web.model.ComponenteWeb;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.PRUEBA_ACTIVADA;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.PRUEBA_NO_ACTIVADA;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.SIN_PRUEBAS;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.doubleFormat;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.formatDate;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.formatDateTime;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.parcentajeMinimoEvaluacion;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author BASP
 */
@Named
@ViewScoped
public class DetallePostulacionView extends ComponenteWeb implements Serializable {

    private static final long serialVersionUID = 1L;

    private Oferta ofertaSeleccionada;
    private Citacion citacionSeleccionada;
    private UsuarioHasOferta usuarioHasOfertaSeleccionada;
    private StreamedContent streamedContent;
    private Citacion newCitacion;
    private Date fechaActual;
    private String porcentajeMinimoEvaluacion;
    private List<PerfilHasPrueba> pruebas;
    private PerfilHasPrueba preubaSeleccionada;
    private Boolean aproboEvaluacion;

    public DetallePostulacionView() {
        newCitacion = new Citacion();
    }

    @PostConstruct
    public void init() {
        try {
            if (getOfertaId() != null) {
                ofertaSeleccionada = ofertaEjb.findOferta(getOfertaId());
            }
            if (getUsuarioHasOfertaId() != null) {
                usuarioHasOfertaSeleccionada = usuarioEjb.findUsuarioHasOferta(getUsuarioHasOfertaId());
                if (usuarioHasOfertaSeleccionada.getCitacion() != null) {
                    citacionSeleccionada = usuarioHasOfertaSeleccionada.getCitacion();
                    pruebas = pruebaEjb.getPruebasByPerfil(usuarioHasOfertaSeleccionada.getOfertasOfertaId().getPerfil().getPerfilId());
                    if (!pruebas.isEmpty()) {
                        Double d = parcentajeMinimoEvaluacion(pruebas);
                        if (citacionSeleccionada.getEvaluacion() != null) {
                            aproboEvaluacion = d < citacionSeleccionada.getEvaluacion().getPorcentaje();
                            porcentajeMinimoEvaluacion = doubleFormat(d);
                        }
                    }
                }
            }
            fechaActual = new Date();
        } catch (BusinessException ex) {
            Logger.getLogger(DetallePostulacionView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void descargarHojaDeVida(String url) {
        streamedContent = descargarArchivoPdf(url);
    }

    public void rechazarCitacion() {
        try {
            usuarioHasOfertaSeleccionada.setEstado(new Catalogo(9L));
            usuarioHasOfertaSeleccionada = usuarioEjb.updateUsuarioHasOferta(usuarioHasOfertaSeleccionada);
            message = webMessage(CITACION_RECHAZADA_OK);
        } catch (BusinessException ex) {
            message = webMessage(CITACION_RECHAZADA_NOT);
            Logger.getLogger(DetallePostulacionView.class.getName()).log(Level.SEVERE, ex.developerException());
        } finally {
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void createCitacion() {
        try {
            Usuario u = getUserLogin();
            usuarioHasOfertaSeleccionada.setEstado(new Catalogo(11L));
            newCitacion.setActivarPruebas(Boolean.FALSE);
            newCitacion.setFechaCreacion(new Date());
            newCitacion.setResuelto(Boolean.FALSE);
            newCitacion.setEstado(Boolean.TRUE);
            newCitacion.setUsuarioAutor(u);
            newCitacion.setUsuarioHasOferta(usuarioHasOfertaSeleccionada);
            citacionSeleccionada = citacionEjb.updateCitacion(newCitacion);
            usuarioHasOfertaSeleccionada = usuarioEjb.updateUsuarioHasOferta(usuarioHasOfertaSeleccionada);
            enviarCitacion(newCitacion, usuarioHasOfertaSeleccionada);
            message = webMessage(CITACION_OK);
        } catch (BusinessException ex) {
            message = webMessage(CITACION_NOT);
            Logger.getLogger(DetallePostulacionView.class.getName()).log(Level.SEVERE, ex.developerException());
        } finally {
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void updateCitacion() {
        try {
            citacionSeleccionada = citacionEjb.updateCitacion(citacionSeleccionada);
            actualizarCitacion(citacionSeleccionada, usuarioHasOfertaSeleccionada);
            message = webMessage(CHANGE_OK);
        } catch (BusinessException ex) {
            message = webMessage(CHANGE_NOT);
            Logger.getLogger(DetallePostulacionView.class.getName()).log(Level.SEVERE, ex.developerException());
        } finally {
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void activarPrueba() {
        try {
            if (!pruebas.isEmpty()) {
                if (citacionEjb.activarPrueba(citacionSeleccionada.getCitacionId()) == 1) {
                    message = webMessage(PRUEBA_ACTIVADA);
                } else {
                    message = webMessage(PRUEBA_NO_ACTIVADA);
                }
            } else {
                message = webMessage(SIN_PRUEBAS);
            }
        } catch (BusinessException ex) {
            message = webMessage(PRUEBA_NO_ACTIVADA);
            Logger.getLogger(DetallePostulacionView.class.getName()).log(Level.SEVERE, ex.developerException());
        } finally {
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public Boolean enviarCitacion(Citacion citacion, UsuarioHasOferta hasOferta) throws BusinessException {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String fechaCitacion = df.format(citacion.getFechaCitacion());
        List<String> lisTo = new ArrayList();
        lisTo.add(hasOferta.getUsuarioUsuarioId().getEmail());
        EmailMessage em = new EmailMessage();
        em.setFrom("trabajoalminuto@gmail.com");
        em.setUser("trabajoalminuto@gmail.com");
        em.setPassword("tam12345");
        em.setPort(587);
        em.setStarttls(Boolean.TRUE);
        em.setMask("Trabajo al minuto");
        em.setSubject("Proceso selección!");
        em.setBodyMessage(BusinessHtmlTemplates.enviarCitacion(hasOferta.getOfertasOfertaId().getUsuarioAutor().getEmpresa().getNombre(), fechaCitacion, hasOferta.getUsuarioUsuarioId().getPersona().getNombre(), citacion.getLugar(), citacion.getDetalles(), hasOferta.getOfertasOfertaId().getPerfil().getTitulo()));
        em.setToList(lisTo);
        em.setHost(HOST_EMAIL_SERVER);
        em.setMimeTypeMessage("text/html; charset=utf-8");
        return sendEmail(em);
    }

    public Boolean actualizarCitacion(Citacion citacion, UsuarioHasOferta hasOferta) throws BusinessException {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String fechaCitacion = df.format(citacion.getFechaCitacion());
        List<String> lisTo = new ArrayList();
        lisTo.add(hasOferta.getUsuarioUsuarioId().getEmail());
        EmailMessage em = new EmailMessage();
        em.setFrom("trabajoalminuto@gmail.com");
        em.setUser("trabajoalminuto@gmail.com");
        em.setPassword("tam12345");
        em.setPort(587);
        em.setStarttls(Boolean.TRUE);
        em.setMask("Trabajo al minuto");
        em.setSubject("Proceso selección!");
        em.setBodyMessage(BusinessHtmlTemplates.citacionRechazada(hasOferta.getOfertasOfertaId().getUsuarioAutor().getEmpresa().getNombre(), fechaCitacion, hasOferta.getUsuarioUsuarioId().getPersona().getNombre(), citacion.getLugar(), citacion.getDetalles()));
        em.setToList(lisTo);
        em.setHost(HOST_EMAIL_SERVER);
        em.setMimeTypeMessage("text/html; charset=utf-8");
        return sendEmail(em);
    }

    public void onRowSelectVerPruebas(SelectEvent event) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("evaluacionId", citacionSeleccionada.getEvaluacion().getEvaluacionId());
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("pruebaperfil", ((PerfilHasPrueba) event.getObject()));
            FacesContext.getCurrentInstance().getExternalContext().redirect(DETALLE_EVALUACION_PAGE);
        } catch (IOException ex) {
            Logger.getLogger(DetallePostulacionView.class.getName()).log(Level.SEVERE, "onRowSelectVerPruebas", ex);
        }
    }

    public String getFormatDate(Date date) {
        if (date != null) {
            return formatDate(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
        }
        return "";
    }

    public String getFormatDateTime(Date date) {
        if (date != null) {
            return formatDateTime(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
        }
        return "";
    }

//    @Getter and Setter
    public Oferta getOfertaSeleccionada() {
        return ofertaSeleccionada;
    }

    public void setOfertaSeleccionada(Oferta ofertaSeleccionada) {
        this.ofertaSeleccionada = ofertaSeleccionada;
    }

    public UsuarioHasOferta getUsuarioHasOfertaSeleccionada() {
        return usuarioHasOfertaSeleccionada;
    }

    public void setUsuarioHasOfertaSeleccionada(UsuarioHasOferta usuarioHasOfertaSeleccionada) {
        this.usuarioHasOfertaSeleccionada = usuarioHasOfertaSeleccionada;
    }

    public StreamedContent getStreamedContent() {
        return streamedContent;
    }

    public void setStreamedContent(StreamedContent streamedContent) {
        this.streamedContent = streamedContent;
    }

    public Citacion getNewCitacion() {
        return newCitacion;
    }

    public void setNewCitacion(Citacion newCitacion) {
        this.newCitacion = newCitacion;
    }

    public Citacion getCitacionSeleccionada() {
        return citacionSeleccionada;
    }

    public void setCitacionSeleccionada(Citacion citacionSeleccionada) {
        this.citacionSeleccionada = citacionSeleccionada;
    }

    public Date getFechaActual() {
        return fechaActual;
    }

    public void setFechaActual(Date fechaActual) {
        this.fechaActual = fechaActual;
    }

    public String getPorcentajeMinimoEvaluacion() {
        return porcentajeMinimoEvaluacion;
    }

    public void setPorcentajeMinimoEvaluacion(String porcentajeMinimoEvaluacion) {
        this.porcentajeMinimoEvaluacion = porcentajeMinimoEvaluacion;
    }

    public List<PerfilHasPrueba> getPruebas() {
        return pruebas;
    }

    public void setPruebas(List<PerfilHasPrueba> pruebas) {
        this.pruebas = pruebas;
    }

    public Boolean getAproboEvaluacion() {
        return aproboEvaluacion;
    }

    public void setAproboEvaluacion(Boolean aproboEvaluacion) {
        this.aproboEvaluacion = aproboEvaluacion;
    }

    public PerfilHasPrueba getPreubaSeleccionada() {
        return preubaSeleccionada;
    }

    public void setPreubaSeleccionada(PerfilHasPrueba preubaSeleccionada) {
        this.preubaSeleccionada = preubaSeleccionada;
    }

}
