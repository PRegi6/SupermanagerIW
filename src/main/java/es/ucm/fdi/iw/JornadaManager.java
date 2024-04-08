package es.ucm.fdi.iw;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JornadaManager {
    @Value("${jornada.actual}")
    private int jornadaActual;

    private static JornadaManager instance;

    private JornadaManager() {}

    @PostConstruct
    private void init() {
        instance = this;
    }

    public static JornadaManager getInstance() {
        return instance;
    }

    public int getJornadaActual() {
        return jornadaActual;
    }

    public void setJornadaActual(int nuevaJornada) {
        this.jornadaActual = nuevaJornada;
    }
}