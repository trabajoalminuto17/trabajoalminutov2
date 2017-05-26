/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.basp.trabajo_al_minuto.web.view;

import static com.basp.trabajo_al_minuto.web.model.AtributosWeb.DETALLE_POSTULACION_PAGE;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.CITACION_RECHAZADA;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.webMessage;

import com.basp.trabajo_al_minuto.service.entity.UsuarioHasOferta;
import com.basp.trabajo_al_minuto.model.business.BusinessException;
import com.basp.trabajo_al_minuto.service.entity.Catalogo;
import com.basp.trabajo_al_minuto.service.entity.Oferta;
import com.basp.trabajo_al_minuto.service.entity.PerfilHasPrueba;
import com.basp.trabajo_al_minuto.service.entity.Usuario;
import static com.basp.trabajo_al_minuto.web.model.AtributosWeb.VER_DETALLE_PRUEBA_PAGE;
import com.basp.trabajo_al_minuto.web.model.ComponenteWeb;
import com.basp.trabajo_al_minuto.web.model.MensajeWeb;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.NO_EVALUADOS;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.OFERTA_APLICADA_NOT;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.OFERTA_APLICADA_OK;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.formatDate;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.formatDateTime;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author BASP
 */
@Named
@ViewScoped
public class DetalleOfertaView extends ComponenteWeb implements Serializable {

    private static final long serialVersionUID = 1L;

    private Oferta ofertaSeleccionada;
    private UsuarioHasOferta usuarioHasOfertaSeleccionada;
    private PerfilHasPrueba preubaSeleccionada;
    private List<UsuarioHasOferta> usuarioHasOfertaFlitradas;
    private List<PerfilHasPrueba> pruebasFlitradas;
    private Usuario usuariologin;
    private UsuarioHasOferta ofertAplicada;
    private StreamedContent streamedContent;

    @PostConstruct
    public void init() {
        try {
            usuariologin = getUserLogin();
            if (getOfertaId() != null) {
                ofertaSeleccionada = ofertaEjb.findOferta(getOfertaId());
            }
            if (usuariologin.getRol().getRolId() == 3L) {
                ofertAplicada = usuarioEjb.getUsuarioHasOfertaByUsuarioAndOferta(usuariologin.getUsuarioId(), ofertaSeleccionada.getOfertaId());
            } else {
                if (getUsuarioHasOfertaId() != null) {
                    usuarioHasOfertaSeleccionada = usuarioEjb.findUsuarioHasOferta(getUsuarioHasOfertaId());
                }
            }
        } catch (BusinessException ex) {
            Logger.getLogger(DetalleOfertaView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<UsuarioHasOferta> getUsuarioHasOfertaByOferta() {
        try {
            return usuarioEjb.getUsuariosByOferta(ofertaSeleccionada.getOfertaId());
        } catch (BusinessException ex) {
            Logger.getLogger(DetalleOfertaView.class.getName()).log(Level.SEVERE, ex.developerException());
            return null;
        }
    }

    public void onRowSelectVerUsuarioHasOfertas(SelectEvent event) {
        try {
            UsuarioHasOferta uho = (UsuarioHasOferta) event.getObject();
            if (uho.getEstado().getCatalogoId() != 9L) {
                if (uho.getEstado().getCatalogoId() == 8L) {
                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("usuarioHasOfertaId", ((UsuarioHasOferta) event.getObject()).getUsuarioHasOfertaId());
                    FacesContext.getCurrentInstance().getExternalContext().redirect(DETALLE_POSTULACION_PAGE);
                } else {
                    if (!uho.getCitacion().getActivarPruebas()) {
                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("usuarioHasOfertaId", ((UsuarioHasOferta) event.getObject()).getUsuarioHasOfertaId());
                        FacesContext.getCurrentInstance().getExternalContext().redirect(DETALLE_POSTULACION_PAGE);
                    } else {
                        message = webMessage(MensajeWeb.USUARIO_EN_PRUEBAS);
                    }
                }
            } else {
                message = webMessage(CITACION_RECHAZADA);
            }
        } catch (IOException ex) {
            Logger.getLogger(DetalleOfertaView.class.getName()).log(Level.SEVERE, "onRowSelectVerUsuarioHasOfertas", ex);
        } finally {
            if (message != null) {
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }

    public List<PerfilHasPrueba> getPruebasByPerfil() {
        try {
            return pruebaEjb.getPruebasByPerfil(ofertaSeleccionada.getPerfil().getPerfilId());
        } catch (BusinessException ex) {
            Logger.getLogger(DetalleOfertaView.class.getName()).log(Level.SEVERE, ex.developerException());
            return null;
        }
    }

    public void onRowSelectVerPruebas(SelectEvent event) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("pruebaId", ((PerfilHasPrueba) event.getObject()).getPrueba().getPruebaId());
            FacesContext.getCurrentInstance().getExternalContext().redirect(VER_DETALLE_PRUEBA_PAGE);
        } catch (IOException ex) {
            Logger.getLogger(DetalleOfertaView.class.getName()).log(Level.SEVERE, "onRowSelectVerPruebas", ex);
        }
    }

    public void aplicarOferta() {
        try {
            UsuarioHasOferta uho = new UsuarioHasOferta();
            uho.setEstado(new Catalogo(8L));
            uho.setFechaPostulacion(new Date());
            uho.setOfertasOfertaId(ofertaSeleccionada);
            uho.setUsuarioUsuarioId(usuariologin);
            ofertAplicada = usuarioEjb.updateUsuarioHasOferta(uho);
            message = webMessage(OFERTA_APLICADA_OK);
        } catch (BusinessException ex) {
            Logger.getLogger(DetalleOfertaView.class.getName()).log(Level.SEVERE, null, ex);
            message = webMessage(OFERTA_APLICADA_NOT);
        } finally {
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public String getFormatDate(Date date) {
        return formatDate(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    public String getFormatDateTime(Date date) {
        if (date != null) {
            return formatDateTime(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
        }
        return "";
    }

    public void downloadReporteCandidatosEvaluados() {
        try {
            streamedContent = null;
            List<UsuarioHasOferta> uho = usuarioEjb.getUsuariosByOferta(ofertaSeleccionada.getOfertaId());
            Integer cant = 0;
            for (UsuarioHasOferta o : uho) {
                if (o.getEstado().getCatalogoId() == 12L) {
                    cant++;
                }
            }
            if (cant > 0) {
                streamedContent = new DefaultStreamedContent(ofertaEjb.getReporteDetalleEvaluados(ofertaSeleccionada.getPerfil().getPerfilId(), cant));
            } else {
                message = webMessage(NO_EVALUADOS);
            }
        } catch (BusinessException ex) {
            Logger.getLogger(DetalleOfertaView.class.getName()).log(Level.SEVERE, ex.developerException());
        } finally {
            if (message != null) {
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }

//    @Getter and Setter
    public Oferta getOfertaSeleccionada() {
        return ofertaSeleccionada;
    }

    public void setOfertaSeleccionada(Oferta ofertaSeleccionada) {
        this.ofertaSeleccionada = ofertaSeleccionada;
    }

    public List<UsuarioHasOferta> getUsuarioHasOfertaFlitradas() {
        return usuarioHasOfertaFlitradas;
    }

    public void setUsuarioHasOfertaFlitradas(List<UsuarioHasOferta> usuarioHasOfertaFlitradas) {
        this.usuarioHasOfertaFlitradas = usuarioHasOfertaFlitradas;
    }

    public UsuarioHasOferta getUsuarioHasOfertaSeleccionada() {
        return usuarioHasOfertaSeleccionada;
    }

    public void setUsuarioHasOfertaSeleccionada(UsuarioHasOferta usuarioHasOfertaSeleccionada) {
        this.usuarioHasOfertaSeleccionada = usuarioHasOfertaSeleccionada;
    }

    public List<PerfilHasPrueba> getPruebasFlitradas() {
        return pruebasFlitradas;
    }

    public void setPruebasFlitradas(List<PerfilHasPrueba> pruebasFlitradas) {
        this.pruebasFlitradas = pruebasFlitradas;
    }

    public PerfilHasPrueba getPreubaSeleccionada() {
        return preubaSeleccionada;
    }

    public void setPreubaSeleccionada(PerfilHasPrueba preubaSeleccionada) {
        this.preubaSeleccionada = preubaSeleccionada;
    }

    public Usuario getUsuariologin() {
        return usuariologin;
    }

    public void setUsuariologin(Usuario usuariologin) {
        this.usuariologin = usuariologin;
    }

    public UsuarioHasOferta getOfertAplicada() {
        return ofertAplicada;
    }

    public void setOfertAplicada(UsuarioHasOferta ofertAplicada) {
        this.ofertAplicada = ofertAplicada;
    }

    public StreamedContent getStreamedContent() {
        return streamedContent;
    }

    public void setStreamedContent(StreamedContent streamedContent) {
        this.streamedContent = streamedContent;
    }

}
