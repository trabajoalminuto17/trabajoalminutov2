/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.basp.trabajo_al_minuto.web.view;

import com.basp.trabajo_al_minuto.model.business.BusinessException;
import com.basp.trabajo_al_minuto.service.entity.Catalogo;
import com.basp.trabajo_al_minuto.service.entity.Oferta;
import com.basp.trabajo_al_minuto.service.entity.Opcion;
import com.basp.trabajo_al_minuto.service.entity.OpcionPlantilla;
import com.basp.trabajo_al_minuto.service.entity.Perfil;
import com.basp.trabajo_al_minuto.service.entity.PerfilHasPrueba;
import com.basp.trabajo_al_minuto.service.entity.Pregunta;
import com.basp.trabajo_al_minuto.service.entity.PreguntaPlantilla;
import com.basp.trabajo_al_minuto.service.entity.Prueba;
import com.basp.trabajo_al_minuto.service.entity.PruebaPlantilla;
import com.basp.trabajo_al_minuto.service.entity.Usuario;
import static com.basp.trabajo_al_minuto.web.model.AtributosWeb.DETALLE_OFERTA_PAGE;
import com.basp.trabajo_al_minuto.web.model.ComponenteWeb;
import static com.basp.trabajo_al_minuto.web.model.MensajeWeb.OFERTA_CREADA_NOT;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.propiedadesItem;
import static com.basp.trabajo_al_minuto.web.model.UtilWeb.webMessage;
import java.io.IOException;
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
import org.primefaces.event.SelectEvent;

/**
 *
 * @author BASP
 */
@Named
@ViewScoped
public class CrearOfertaView extends ComponenteWeb implements Serializable {

    private static final long serialVersionUID = 1L;

    private Oferta newOferta;
    private Perfil newPerfil;
    private Long area;
    private PruebaPlantilla pruebaPlantillaSeleccionada;
    private PerfilHasPrueba pruebaSeleccionada;
    private List<PruebaPlantilla> pruebasSource;
    private List<PerfilHasPrueba> pruebasTarget;
    private Double valor;
    private Double valorMinimo;

    public CrearOfertaView() {
        newOferta = new Oferta();
        newPerfil = new Perfil();
    }

    @PostConstruct
    public void init() {
        try {
            Usuario u = getUserLogin();
            pruebasTarget = new ArrayList();
            pruebasSource = pruebaEjb.getPruebasPlantillaByEmpresa(u.getEmpresa().getEmpresaId());
        } catch (BusinessException ex) {
            Logger.getLogger(DetallePruebaView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void createOferta() {
        try {
            newPerfil.setTitulo(newPerfil.getTitulo());
            newPerfil.setDescripcion(newPerfil.getDescripcion());
            newPerfil.setSalario(newPerfil.getSalario());
            newPerfil.setRequerimientos(newPerfil.getRequerimientos());
            newPerfil.setArea(new Catalogo(area));
            newOferta.setPerfil(newPerfil);
            newOferta.setUsuarioAutor(getUserLogin());
            newOferta.setFechaExpiracion(newOferta.getFechaExpiracion());
            newOferta.setEstado(Boolean.TRUE);
            newPerfil.setPerfilHasPruebaList(pruebasTarget);
            Oferta o = ofertaEjb.updateOferta(newOferta);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ofertaId", o.getOfertaId());
            FacesContext.getCurrentInstance().getExternalContext().redirect(DETALLE_OFERTA_PAGE);
        } catch (BusinessException ex) {
            message = webMessage(OFERTA_CREADA_NOT);
            Logger.getLogger(CrearOfertaView.class.getName()).log(Level.SEVERE, ex.developerException());
        } catch (IOException ex) {
            Logger.getLogger(CrearOfertaView.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (message != null) {
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
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

    public void onRowSelectPruebasPlantilla(PruebaPlantilla pp, Double valor) {
        PerfilHasPrueba php = new PerfilHasPrueba();
        Prueba p = new Prueba();
        p.setPruebaId(pp.getPruebaId());
        p.setArea(pp.getArea());
        p.setDescripcion(pp.getDescripcion());
        p.setEstado(Boolean.TRUE);
        p.setLimiteTiempo(pp.getLimiteTiempo());
        p.setNombre(pp.getNombre());
        p.setPorcentajeMinimo(pp.getPorcentajeMinimo());
        p.setPreguntaList(setPreguntas(pp, p));
        php.setPerfilHasPruebaId(pp.getPruebaId().intValue());
        php.setPerfil(newPerfil);
        php.setPrueba(p);
        php.setPorcentaje(valor);
        pruebasTarget.add(php);
        pruebasSource.remove(pp);

    }

    public void onRowSelectPruebas(SelectEvent event) {
        PerfilHasPrueba php = (PerfilHasPrueba) event.getObject();
        PruebaPlantilla pp = new PruebaPlantilla();
        pp.setArea(php.getPrueba().getArea());
        pp.setDescripcion(php.getPrueba().getDescripcion());
        pp.setEmpresaEmpresaId(getUserLogin().getEmpresa());
        pp.setEstado(Boolean.TRUE);
        pp.setLimiteTiempo(php.getPrueba().getLimiteTiempo());
        pp.setNombre(php.getPrueba().getNombre());
        pp.setPorcentajeMinimo(php.getPrueba().getPorcentajeMinimo());
        pp.setPruebaId(php.getPrueba().getPruebaId());
        pruebasSource.add(pp);
        pruebasTarget.remove(php);

    }

    public void validateMinValue() {
        valorMinimo = 0D;
        for (PerfilHasPrueba php : pruebasTarget) {
            valorMinimo = valorMinimo + php.getPorcentaje();
        }
        valorMinimo = 100 - valorMinimo;
        valor = valorMinimo;
    }

    private List<Pregunta> setPreguntas(PruebaPlantilla pp, Prueba p) {
        List<Pregunta> preguntas = new ArrayList();
        for (PreguntaPlantilla pregunta : pp.getPreguntaPlantillaList()) {
            Pregunta pe = new Pregunta();
            pe.setEnunciado(pregunta.getEnunciado());
            pe.setOpcionList(setOpciones(pregunta, pe));
            pe.setPruebaPruebaId(p);
            preguntas.add(pe);
        }
        return preguntas;
    }

    private List<Opcion> setOpciones(PreguntaPlantilla pp, Pregunta p) {
        List<Opcion> opciones = new ArrayList();
        for (OpcionPlantilla o : pp.getOpcionPlantillaList()) {
            Opcion oe = new Opcion();
            oe.setEnunciado(o.getEnunciado());
            oe.setCorrecta(o.getCorrecta());
            oe.setPreguntaPreguntaId(p);
            opciones.add(oe);
        }
        return opciones;
    }

    public Oferta getNewOferta() {
        return newOferta;
    }

    public void setNewOferta(Oferta newOferta) {
        this.newOferta = newOferta;
    }

    public Perfil getNewPerfil() {
        return newPerfil;
    }

    public void setNewPerfil(Perfil newPerfil) {
        this.newPerfil = newPerfil;
    }

    public Long getArea() {
        return area;
    }

    public void setArea(Long area) {
        this.area = area;
    }

    public List<PruebaPlantilla> getPruebasSource() {
        return pruebasSource;
    }

    public void setPruebasSource(List<PruebaPlantilla> pruebasSource) {
        this.pruebasSource = pruebasSource;
    }

    public List<PerfilHasPrueba> getPruebasTarget() {
        return pruebasTarget;
    }

    public void setPruebasTarget(List<PerfilHasPrueba> pruebasTarget) {
        this.pruebasTarget = pruebasTarget;
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

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public Double getValorMinimo() {
        return valorMinimo;
    }

    public void setValorMinimo(Double valorMinimo) {
        this.valorMinimo = valorMinimo;
    }

}
