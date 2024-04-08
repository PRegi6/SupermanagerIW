package es.ucm.fdi.iw.model;

import javax.persistence.*;

import lombok.Data;

@NamedQueries({
    @NamedQuery(name="PartidoACB.partidosJornada",
    query="SELECT p FROM PartidoACB p "
            + "WHERE p.jornada = :jornada"),
    @NamedQuery(name="PartidoACB.equipos",
    query="SELECT DISTINCT p.local FROM PartidoACB p")
})
@Entity
@Data
public class PartidoACB {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;
    
    // @Column(nullable = false, unique = true)
    private String local;

    private int puntosLocal;

    // @Column(nullable = false, unique = true)
    private String visitante;

    private int puntosVisitante;

    // @Column(nullable = false, unique = true)
    private int jornada;
}
