/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.basp.trabajo_al_minuto.web.view;

import com.basp.trabajo_al_minuto.model.business.BusinessException;
import com.basp.trabajo_al_minuto.model.dto.OpcionEmpresa;
import com.basp.trabajo_al_minuto.model.dto.PreguntaEmpresa;
import com.basp.trabajo_al_minuto.model.dto.PruebaEmpresa;
import com.basp.trabajo_al_minuto.service.entity.Catalogo;
import com.basp.trabajo_al_minuto.service.entity.Empresa;
import com.basp.trabajo_al_minuto.service.entity.OpcionPlantilla;
import com.basp.trabajo_al_minuto.service.entity.PreguntaPlantilla;
import com.basp.trabajo_al_minuto.service.entity.PruebaPlantilla;
import com.basp.trabajo_al_minuto.web.model.ComponenteWeb;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.CHANGE_NOT;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.CHANGE_OK;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.QUESTION_NULL;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.QUESTION_REPEAT;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.propiedadesItem;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.webMessage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author BASP
 */
@Named
@ViewScoped
public class DetallePruebaView extends ComponenteWeb implements Serializable {

    private static final long serialVersionUID = 1L;

    private PruebaEmpresa pruebaSeleccionada;
    private Long area;

    @PostConstruct
    public void init() {
        try {
            if (getPruebaPlantillaId() != null) {
                PruebaPlantilla pp = pruebaEjb.findPruebaPlantilla(getPruebaPlantillaId());
                area = pp.getArea().getCatalogoId();
                pruebaSeleccionada = setPruebaEmpresa(pp);
            }
        } catch (BusinessException ex) {
            Logger.getLogger(DetallePruebaView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<SelectItem> getAreas() {
        List<SelectItem> response = new ArrayList();
        response.add(propiedadesItem(new SelectItem(-1, "Seleccione area..")));
        try {
            List<Catalogo> list = getCatalogosByParent(1L);
            for (Catalogo c : list) {
                response.add(new SelectItem(c.getCatalogoId(), c.getValor()));
            }
        } catch (BusinessException ex) {
            Logger.getLogger(DetallePruebaView.class.getName()).log(Level.SEVERE, ex.developerException());
        }
        return response;
    }

    public void addPregunta() {
        PreguntaEmpresa pe = new PreguntaEmpresa();
        List<OpcionEmpresa> opciones = new ArrayList();
        opciones.add(addOpcion());
        opciones.add(addOpcion());
        pe.setEnunciado("Ingrese enunciado");
        pe.setOpciones(opciones);
        pruebaSeleccionada.getPreguntas().add(pe);
    }

    public OpcionEmpresa addOpcion() {
        OpcionEmpresa op = new OpcionEmpresa();
        op.setEnunciado("Ingrese enunciado");
        op.setCorrecta(Boolean.FALSE);
        return op;
    }

    public void addOpcion(PreguntaEmpresa pregunta) {
        OpcionEmpresa op = new OpcionEmpresa();
        op.setEnunciado("Ingrese enunciado");
        op.setCorrecta(Boolean.FALSE);
        pregunta.getOpciones().add(op);
    }

    public void removePregunta(PreguntaEmpresa pregunta) {
        pruebaSeleccionada.getPreguntas().remove(pregunta);
    }

    public void removeOpcion(PreguntaEmpresa pregunta, OpcionEmpresa opcion) {
        for (PreguntaEmpresa pp : pruebaSeleccionada.getPreguntas()) {
            if (pp.equals(pregunta)) {
                pp.getOpciones().remove(opcion);
            }
        }
    }

    public void updatePruebaPlantilla() {
        try {
            message = null;
            PruebaPlantilla pp = setPruebaPlantilla(pruebaSeleccionada);
            if (message == null) {
                pruebaSeleccionada = setPruebaEmpresa(pruebaEjb.updatePruebaPlantilla(pp));
                message = webMessage(CHANGE_OK);
            }
        } catch (BusinessException ex) {
            message = webMessage(CHANGE_NOT);
            Logger.getLogger(DetallePruebaView.class.getName()).log(Level.SEVERE, ex.developerException());
        } finally {
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    private PruebaEmpresa setPruebaEmpresa(PruebaPlantilla p) {
        PruebaEmpresa pe = new PruebaEmpresa();
        pe.setArea(area);
        pe.setDescripcion(p.getDescripcion());
        pe.setNombre(p.getNombre());
        pe.setId(p.getPruebaId());
        pe.setPorcentaje(p.getPorcentajeMinimo());
        pe.setTiempo(p.getLimiteTiempo());
        pe.setPreguntas(setPreguntasEmpresa(p));
        pe.setEmpresa(p.getEmpresaEmpresaId().getEmpresaId());
        pe.setEstado(p.getEstado());
        return pe;
    }

    private PruebaPlantilla setPruebaPlantilla(PruebaEmpresa p) {
        PruebaPlantilla pe = new PruebaPlantilla();
        pe.setArea(new Catalogo(area));
        pe.setDescripcion(p.getDescripcion());
        pe.setNombre(p.getNombre());
        pe.setPruebaId(p.getId());
        pe.setPorcentajeMinimo(p.getPorcentaje());
        pe.setLimiteTiempo(p.getTiempo());
        pe.setPreguntaPlantillaList(setPreguntasEmpresa(p));
        pe.setEmpresaEmpresaId(new Empresa(p.getEmpresa()));
        pe.setEstado(p.getEstado());
        return pe;
    }

    private List<PreguntaEmpresa> setPreguntasEmpresa(PruebaPlantilla p) {
        List<PreguntaEmpresa> preguntas = new ArrayList();
        for (PreguntaPlantilla pregunta : p.getPreguntaPlantillaList()) {
            PreguntaEmpresa pe = new PreguntaEmpresa();
            pe.setEnunciado(pregunta.getEnunciado());
            pe.setId(pregunta.getPreguntaId());
            pe.setOpciones(setOpcionesEmpresa(pregunta));
            preguntas.add(pe);
        }
        return preguntas;
    }

    private List<PreguntaPlantilla> setPreguntasEmpresa(PruebaEmpresa p) {
        List<PreguntaPlantilla> preguntas = new ArrayList();
        for (PreguntaEmpresa pregunta : p.getPreguntas()) {
            if (validarPreguntaNull(pregunta)) {
                if (!validarPreguntaRepeat(pregunta)) {
                    PreguntaPlantilla pe = new PreguntaPlantilla();
                    pe.setEnunciado(pregunta.getEnunciado());
                    pe.setPreguntaId(pregunta.getId());
                    pe.setOpcionPlantillaList(setOpcionesPlantilla(pregunta));
                    pe.setPruebaPlantillaPruebaId(new PruebaPlantilla(p.getId()));
                    preguntas.add(pe);
                } else {
                    message = webMessage(QUESTION_REPEAT);
                }
            } else {
                message = webMessage(QUESTION_NULL);
            }
        }
        return preguntas;
    }

    private List<OpcionEmpresa> setOpcionesEmpresa(PreguntaPlantilla p) {
        List<OpcionEmpresa> opciones = new ArrayList();
        for (OpcionPlantilla o : p.getOpcionPlantillaList()) {
            OpcionEmpresa oe = new OpcionEmpresa();
            oe.setEnunciado(o.getEnunciado());
            oe.setCorrecta(o.getCorrecta());
            oe.setId(o.getOpcionId());
            opciones.add(oe);
        }
        return opciones;
    }

    private List<OpcionPlantilla> setOpcionesPlantilla(PreguntaEmpresa p) {
        List<OpcionPlantilla> opciones = new ArrayList();
        for (OpcionEmpresa o : p.getOpciones()) {
            OpcionPlantilla oe = new OpcionPlantilla();
            oe.setEnunciado(o.getEnunciado());
            oe.setCorrecta(o.getCorrecta());
            oe.setOpcionId(o.getId());
            oe.setPreguntaPlantillaPreguntaId(new PreguntaPlantilla(p.getId()));
            opciones.add(oe);
        }
        return opciones;
    }

    private Boolean validarPreguntaNull(PreguntaEmpresa pe) {
        Boolean b = Boolean.FALSE;
        for (OpcionEmpresa oe : pe.getOpciones()) {
            b = oe.getCorrecta();
            if (b) {
                break;
            }
        }
        return b;

    }

    private Boolean validarPreguntaRepeat(PreguntaEmpresa pe) {
        Integer cont = 0;
        for (OpcionEmpresa oe : pe.getOpciones()) {
            if (oe.getCorrecta()) {
                cont++;
            }
            if (cont > 1) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;

    }

//    @Getter and Setter
    public PruebaEmpresa getPruebaSeleccionada() {
        return pruebaSeleccionada;
    }

    public void setPruebaSeleccionada(PruebaEmpresa pruebaSeleccionada) {
        this.pruebaSeleccionada = pruebaSeleccionada;
    }

    public Long getArea() {
        return area;
    }

    public void setArea(Long area) {
        this.area = area;
    }
}
