/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.basp.trabajo_al_minuto.web.view;

import com.basp.trabajo_al_minuto.model.business.BusinessException;
import com.basp.trabajo_al_minuto.model.dto.OpcionEvaluacion;
import com.basp.trabajo_al_minuto.model.dto.PreguntaEvaluacion;
import com.basp.trabajo_al_minuto.model.dto.PruebaEvaluacion;
import com.basp.trabajo_al_minuto.service.entity.Evaluacion;
import com.basp.trabajo_al_minuto.service.entity.Opcion;
import com.basp.trabajo_al_minuto.service.entity.PerfilHasPrueba;
import com.basp.trabajo_al_minuto.service.entity.Pregunta;
import com.basp.trabajo_al_minuto.service.entity.Prueba;
import com.basp.trabajo_al_minuto.service.entity.Respuesta;
import static com.basp.trabajo_al_minuto.web.model.AtributosWeb.PRUEBA_PLANTILLA_PAGE;
import com.basp.trabajo_al_minuto.web.model.ComponenteWeb;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author BASP
 */
@Named
@ViewScoped
public class EvaluacionView extends ComponenteWeb implements Serializable {

    private static final long serialVersionUID = 1L;

    private PruebaEvaluacion pruebaEvaluacion;
    private Evaluacion evaluacion;

    @PostConstruct
    public void init() {
        try {
            if (getEvaluacionId() != null && getPruebaPerfil() != null) {
                pruebaEvaluacion = new PruebaEvaluacion();
                evaluacion = pruebaEjb.findEvaluacion(getEvaluacionId());
                List<PerfilHasPrueba> pruebasresueltas = getPruebasResueltas();
                pruebasresueltas.add(getPruebaPerfil());
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("pruebasresueltas", pruebasresueltas);
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("pruebaperfil", getPruebaPerfil());
                pruebaEvaluacion = setPrueba(getPruebaPerfil().getPrueba());
            }
        } catch (BusinessException ex) {
            Logger.getLogger(EvaluacionView.class.getName()).log(Level.SEVERE, ex.developerException());
        }
    }

    public void finalizarPrueba() {
        onTimeout();
    }

    public void onTimeout() {
        try {
            List<Respuesta> respuestas = new ArrayList();
            for (PreguntaEvaluacion pe : pruebaEvaluacion.getPreguntas()) {
                Boolean a = validarPreguntaNull(pe);
                Boolean b = validarPreguntaRepeat(pe);
                Respuesta r = new Respuesta();
                if (!a || b) {
                    Opcion o = new Opcion();
                    o.setCorrecta(Boolean.FALSE);
                    o.setEnunciado("Incorrecta o no respondida");
                    o.setPreguntaPreguntaId(new Pregunta(pe.getId()));
                    o = pruebaEjb.updateOpcion(o);
                    r.setEvaluacion(evaluacion);
                    r.setOpcion(o);
                } else {
                    for (OpcionEvaluacion oe : pe.getOpciones()) {
                        if (oe.getRespuesta()) {
                            r.setEvaluacion(evaluacion);
                            r.setOpcion(new Opcion(oe.getId()));
                            break;
                        }
                    }
                }
                respuestas.add(r);
            }
            if (!respuestas.isEmpty()) {
                for (Respuesta r : respuestas) {
                    pruebaEjb.updateRespuesta(r);
                }
            }
            FacesContext.getCurrentInstance().getExternalContext().redirect(PRUEBA_PLANTILLA_PAGE);
        } catch (BusinessException ex) {
            Logger.getLogger(EvaluacionView.class.getName()).log(Level.SEVERE, ex.developerException());
        } catch (IOException ex) {
            Logger.getLogger(EvaluacionView.class.getName()).log(Level.SEVERE, "onTimeout", ex);
        } finally {
            if (message != null) {
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }

    private PruebaEvaluacion setPrueba(Prueba p) {
        PruebaEvaluacion pe = new PruebaEvaluacion();
        pe.setArea(p.getArea().getValor());
        pe.setAreaId(p.getArea().getCatalogoId());
        pe.setDescripcion(p.getDescripcion());
        pe.setNombre(p.getNombre());
        pe.setId(p.getPruebaId());
        pe.setPorcentaje(p.getPorcentajeMinimo());
        pe.setTiempo(getFormatDate(p.getLimiteTiempo()));
        pe.setPreguntas(setPreguntas(p));
        return pe;
    }

    private List<PreguntaEvaluacion> setPreguntas(Prueba p) {
        List<PreguntaEvaluacion> preguntas = new ArrayList();
        for (Pregunta pregunta : p.getPreguntaList()) {
            PreguntaEvaluacion pe = new PreguntaEvaluacion();
            pe.setEnunciado(pregunta.getEnunciado());
            pe.setId(pe.getId());
            pe.setOpciones(setOpciones(pregunta));
            preguntas.add(pe);
        }
        return preguntas;
    }

    private List<OpcionEvaluacion> setOpciones(Pregunta p) {
        List<OpcionEvaluacion> opciones = new ArrayList();
        for (Opcion o : p.getOpcionList()) {
            OpcionEvaluacion oe = new OpcionEvaluacion();
            oe.setEnunciado(o.getEnunciado());
            oe.setCorrecta(o.getCorrecta());
            oe.setId(o.getOpcionId());
            oe.setRespuesta(Boolean.FALSE);
            opciones.add(oe);
        }
        return opciones;
    }

    private Boolean validarPreguntaNull(PreguntaEvaluacion pe) {
        Boolean b = Boolean.FALSE;
        for (OpcionEvaluacion oe : pe.getOpciones()) {
            b = oe.getRespuesta();
            if (b) {
                break;
            }
        }
        return b;

    }

    private Boolean validarPreguntaRepeat(PreguntaEvaluacion pe) {
        Integer cont = 0;
        for (OpcionEvaluacion oe : pe.getOpciones()) {
            if (oe.getRespuesta()) {
                cont++;
            }
            if (cont > 1) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;

    }

    public Long getFormatDate(Date date) {
        if (date != null) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toEpochSecond(ZoneOffset.UTC);
        }
        return 0L;
    }

//    @Getter and Setter
    public PruebaEvaluacion getPruebaEvaluacion() {
        return pruebaEvaluacion;
    }

    public void setPruebaEvaluacion(PruebaEvaluacion pruebaEvaluacion) {
        this.pruebaEvaluacion = pruebaEvaluacion;
    }

}
