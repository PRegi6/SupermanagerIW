package es.ucm.fdi.iw.controller;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.ucm.fdi.iw.model.Equipo;
import es.ucm.fdi.iw.model.EquipoACB;
import es.ucm.fdi.iw.model.Jornada;
import es.ucm.fdi.iw.model.JugadorACB;
import es.ucm.fdi.iw.model.Message;
import es.ucm.fdi.iw.model.PartidoACB;
import es.ucm.fdi.iw.model.PuntosEquipo;
import es.ucm.fdi.iw.model.PuntosJugador;
import es.ucm.fdi.iw.model.User;

/**
 *  Site administration.
 *
 *  Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("admin")
public class AdminController {

	private static final Logger log = LogManager.getLogger(AdminController.class);

    @Autowired
	private EntityManager entityManager;

	@GetMapping("/")
    public String index(Model model) {
        List<Message> reportedMessages = entityManager.createNamedQuery("Message.reportados", Message.class)
            .getResultList();
        model.addAttribute("reportados", reportedMessages);
        return "admin";
    }

    @GetMapping("/clasificacion")
    public String getClasificacion() { return "clasificacion"; }

    @GetMapping("/equipos")
    public String getEquipos() { return "equipos"; }

    @GetMapping("/mercado")
    public String getMercado() { return "mercado"; }

    @GetMapping("/actualidad")
    public String getActualidad() { return "actualidad"; }

    @GetMapping("/miequipo")
    public String getMiequipo() { return "miequipo"; }

    @GetMapping("/jugador")
    public String getJugador() { return "jugador"; }

    @GetMapping("/liga")
    public String getLiga() { return "liga"; }

    @GetMapping("/foro")
    public String getForo() { return "foro"; }

    @GetMapping("/crearliga")
    public String getCrearLiga() { return "crearliga"; }

    @GetMapping("/usuarios")
    public String getUsuarios(Model model) {
        List<User> usuarios = entityManager.createNamedQuery("User.allUsers", User.class)
            .getResultList();

        model.addAttribute("usuarios", usuarios);
        return "usuarios"; 
    }

    @GetMapping("/mensajesReportados")
    public String mensajesReportados(Model model) {
        List<Message> reportedMessages = entityManager.createNamedQuery("Message.reportados", Message.class)
            .getResultList();
        model.addAttribute("reportados", reportedMessages);
        
        return "mensajesReportados";
    }

    @GetMapping("/mensajesUsuario")
    public String mensajesReportados(@RequestParam("idUsuario") Long idUsuario, Model model) {
        User u = entityManager.find(User.class, idUsuario);

        List<Message> mensajesUsuario = u.getSent();

        model.addAttribute("usuario", u);
        model.addAttribute("mensajes", mensajesUsuario);
        
        return "mensajesUsuario";
    }

    @Transactional
    public void actualizarDatos() {
        Jornada jornada = entityManager.createNamedQuery("Jornada.getJornada", Jornada.class).getSingleResult();

        // Actualizamos las estadísticas de los jugadores
        List<JugadorACB> jugadores = entityManager.createNamedQuery("JugadorACB.jugadores", JugadorACB.class)
            .getResultList();

        for (JugadorACB j: jugadores) {
            try {
                PuntosJugador pJ = entityManager.createNamedQuery("PuntosJugador.jugador", PuntosJugador.class)
                    .setParameter("nombre", j.getNombre())
                    .setParameter("jornada", jornada.getJornada())
                    .getSingleResult();

                int partidosJugados = j.getPartidosJugados();
                int puntosJugador = j.getPuntosTotales();
                int val_jugador = j.getValoracionTotal();

                j.setPartidosJugados(partidosJugados + 1);
                j.setPuntosTotales(puntosJugador + pJ.getPuntos());
                j.setValorMercado(pJ.getValor_mercado());
                j.setValoracionTotal(val_jugador + pJ.getValoracion());
                double valoracionMedia = Math.round((j.getValoracionTotal() / j.getPartidosJugados()));
                j.setValoracionMedia(valoracionMedia);

                entityManager.flush();
            } catch (NoResultException e) { /*Si la consulta no devuelve un resultado sigo mirando*/ }
        }

        // Actualizamos los puntos de todos los equipos de los usuarios
        List<Equipo> equipos = entityManager.createNamedQuery("Equipo.todosEquipos", Equipo.class)
            .getResultList();
        
        for (Equipo e: equipos) {
            PuntosEquipo pE = new PuntosEquipo();
            pE.setEquipo(e);
            pE.setJornada(jornada.getJornada());
            List<JugadorACB> jugadoresEquipo = e.getJugadores();
            int puntosGanados = 0;
            for (JugadorACB j: jugadoresEquipo) {
                try {
                    PuntosJugador pJ = entityManager.createNamedQuery("PuntosJugador.jugador", PuntosJugador.class)
                        .setParameter("nombre", j.getNombre())
                        .setParameter("jornada", jornada.getJornada())
                            .getSingleResult();

                    puntosGanados = puntosGanados + pJ.getValoracion();

                    pE.setPuntos(puntosGanados);
                    pE.getJugadores().add(pJ);
                } catch (NoResultException excep) { /*Si la consulta no devuelve un resultado sigo mirando*/ }
            }

            e.setPuntos(e.getPuntos() + puntosGanados);
            entityManager.persist(pE);
            entityManager.flush();
        }

        List<PartidoACB> partidos = entityManager.createNamedQuery("PartidoACB.partidosJornada", PartidoACB.class)
            .setParameter("jornada", jornada.getJornada())
                .getResultList();
        
        for (PartidoACB p: partidos) {
            EquipoACB local = entityManager.createNamedQuery("EquipoACB.equipo", EquipoACB.class)
                .setParameter("equipo", p.getLocal())
                    .getSingleResult();

            EquipoACB visitante = entityManager.createNamedQuery("EquipoACB.equipo", EquipoACB.class)
                .setParameter("equipo", p.getVisitante())
                    .getSingleResult();

            int puntosLocal = p.getPuntosLocal();
            int puntosVisitante = p.getPuntosVisitante();
            local.setPuntosAFavor(local.getPuntosAFavor() + puntosLocal);
            local.setPuntosEnContra(local.getPuntosEnContra() + puntosVisitante);
            visitante.setPuntosAFavor(visitante.getPuntosAFavor() + puntosVisitante);
            visitante.setPuntosEnContra(visitante.getPuntosEnContra() + puntosLocal);

            if (puntosLocal > puntosVisitante) {
                local.setVictorias(local.getVictorias() + 1);
                visitante.setDerrotas(visitante.getDerrotas() + 1);
            }
            else {
                visitante.setVictorias(visitante.getVictorias() + 1);
                local.setDerrotas(local.getDerrotas() + 1);
            }

            local.setDiferencia(local.getPuntosAFavor() - local.getPuntosEnContra());
            visitante.setDiferencia(visitante.getPuntosAFavor() - visitante.getPuntosEnContra());

            entityManager.flush();
        }
    }

    @PostMapping("/banearUsuario")
    @Transactional
    public String banearUsuario(@RequestParam("idUsuario") Long idUsuario, Model model) {
        User u = entityManager.find(User.class, idUsuario);
        u.setEnabled(false);

        return index(model);
    }

    @PostMapping("/desbanearUsuario")
    @Transactional
    public String desbanearUsuario(@RequestParam("idUsuario") Long idUsuario, Model model) {
        User u = entityManager.find(User.class, idUsuario);
        u.setEnabled(true);

        return index(model);
    }

    @PostMapping("/eliminarMensaje")
    @Transactional
    public String eliminarMensaje( @RequestParam("idMensaje") Long idMensaje, Model model) {
        Message m = entityManager.find(Message.class, idMensaje);

        entityManager.remove(m);

        return index(model);
    }

    @PostMapping("/validarMensaje")
    @Transactional
    public String validarMensaje(@RequestParam("idMensaje") Long idMensaje, Model model) {

        Message m = entityManager.find(Message.class, idMensaje);

        m.setReported(false);

        return index(model);
    }

    @PostMapping("/avanzarJornada")
    @Transactional
    @ResponseBody
    public String avanzarJornada(Model model) {
        Jornada jornada = entityManager.createNamedQuery("Jornada.getJornada", Jornada.class).getSingleResult();
        
        actualizarDatos();

        jornada.setJornada(jornada.getJornada() + 1);

        model.addAttribute("jornada", jornada.getJornada());

        return "{\"result\": \"jornada avanzada correctamente.\"}";
    }

    @PostMapping("/eliminarMensajes")
    @Transactional
    public String eliminarMensajes(@RequestParam("idUsuario") Long idUsuario, Model model) {
        User u = entityManager.find(User.class, idUsuario);

        u.getSent().clear();

        model.addAttribute("usuario", u);
        model.addAttribute("mensajes", u.getSent());

        return "mensajesUsuario";
    }

}
