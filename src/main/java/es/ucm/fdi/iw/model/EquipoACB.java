package es.ucm.fdi.iw.model;

import javax.persistence.*;

import lombok.Data;

@Entity
@Data
@NamedQueries({
    @NamedQuery(name="EquipoACB.equipos",
    query="SELECT e FROM EquipoACB e"),
    @NamedQuery(name="EquipoACB.equipo",
    query="SELECT e FROM EquipoACB e "
            + "WHERE e.nombreEquipo = :equipo")
})
@Table(name="EquipoACB")
public class EquipoACB {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    // @Column(nullable = false, unique = true)
    private String nombreEquipo;

    private int victorias;
    private int derrotas;
    private int puntosAFavor;
    private int puntosEnContra;
    private int diferencia;

}