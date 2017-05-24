/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.basp.trabajo_al_minuto.web.view;

import com.basp.trabajo_al_minuto.model.business.BusinessException;
import com.basp.trabajo_al_minuto.service.entity.Evaluacion;
import com.basp.trabajo_al_minuto.service.entity.PerfilHasPrueba;
import com.basp.trabajo_al_minuto.service.entity.Respuesta;
import com.basp.trabajo_al_minuto.web.model.ComponenteWeb;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.doubleFormat;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.evaluarPrueba;
import java.io.Serializable;
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
public class DetalleEvaluacionView extends ComponenteWeb implements Serializable {

    private static final long serialVersionUID = 1L;

    private PerfilHasPrueba perfilHasPrueba;
    private Evaluacion evaluacion;
    private List<Respuesta> respuestas;
    private String porcentajeresultado;
    private Boolean aproboPrueba;

    @PostConstruct
    public void init() {
        if (getPruebaPerfil() != null && getEvaluacionId() != null) {
            try {
                perfilHasPrueba = getPruebaPerfil();
                evaluacion = pruebaEjb.findEvaluacion(getEvaluacionId());
                respuestas = citacionEjb.getRespuestasByEvaluacionPrueba(evaluacion.getEvaluacionId(), perfilHasPrueba.getPrueba().getPruebaId());
                Double d = evaluarPrueba(respuestas, perfilHasPrueba.getPrueba());
                aproboPrueba = perfilHasPrueba.getPrueba().getPorcentajeMinimo() < d;
                porcentajeresultado = doubleFormat(d);
            } catch (BusinessException ex) {
                Logger.getLogger(DetalleEvaluacionView.class.getName()).log(Level.SEVERE, ex.developerException());
            }
        }
    }

//    @Getter and Setter
    public PerfilHasPrueba getPerfilHasPrueba() {
        return perfilHasPrueba;
    }

    public void setPerfilHasPrueba(PerfilHasPrueba perfilHasPrueba) {
        this.perfilHasPrueba = perfilHasPrueba;
    }

    public Evaluacion getEvaluacion() {
        return evaluacion;
    }

    public void setEvaluacion(Evaluacion evaluacion) {
        this.evaluacion = evaluacion;
    }

    public List<Respuesta> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<Respuesta> respuestas) {
        this.respuestas = respuestas;
    }

    public String getPorcentajeresultado() {
        return porcentajeresultado;
    }

    public void setPorcentajeresultado(String porcentajeresultado) {
        this.porcentajeresultado = porcentajeresultado;
    }

    public Boolean getAproboPrueba() {
        return aproboPrueba;
    }

    public void setAproboPrueba(Boolean aproboPrueba) {
        this.aproboPrueba = aproboPrueba;
    }

}
