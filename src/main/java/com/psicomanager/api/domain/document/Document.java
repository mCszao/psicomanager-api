package com.psicomanager.api.domain.document;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.psicomanager.api.domain.patient.Patient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity(name = "documents")
@Table(name = "documents", uniqueConstraints = {
        @UniqueConstraint(columnNames = "NAME")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @NotNull
    @Size(max = 255)
    @Column(name = "NAME", nullable = false, unique = true, length = 255)
    private String name;

    @ManyToOne
    @JoinColumn(name = "patient" , nullable = false)
    private Patient patient ;

    @NotNull
    @Size(max = 10)
    @Column(name = "TYPE", nullable = true, unique = false, length = 10)
    private String type;

    @NotNull
    @Lob
    @Column(name = "CONTENT", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] content;
}
