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
import com.basp.trabajo_al_minuto.service.entity.Opcion;
import com.basp.trabajo_al_minuto.service.entity.Pregunta;
import com.basp.trabajo_al_minuto.service.entity.Prueba;
import com.basp.trabajo_al_minuto.web.model.ComponenteWeb;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.formatDateTime;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author Bryan.Silva
 */
@Named
@ViewScoped
public class VerDetallePruebaView extends ComponenteWeb implements Serializable {

    private static final long serialVersionUID = 1L;
    private PruebaEvaluacion pruebaSeleccionada;
    private String tiempo;

    @PostConstruct
    public void init() {
        try {
            if (getPruebaId() != null) {
                Prueba p = pruebaEjb.findPrueba(getPruebaId());
                tiempo = getFormatDate(p.getLimiteTiempo());
                pruebaSeleccionada = setPrueba(p);
            }
        } catch (BusinessException ex) {
            Logger.getLogger(DetallePruebaView.class.getName()).log(Level.SEVERE, null, ex);
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

    public String getFormatDate(Date date) {
        if (date != null) {
            return formatDateTime(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
        }
        return "";
    }

    public PruebaEvaluacion getPruebaSeleccionada() {
        return pruebaSeleccionada;
    }

    public void setPruebaSeleccionada(PruebaEvaluacion pruebaSeleccionada) {
        this.pruebaSeleccionada = pruebaSeleccionada;
    }

    public String getTiempo() {
        return tiempo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }

}
