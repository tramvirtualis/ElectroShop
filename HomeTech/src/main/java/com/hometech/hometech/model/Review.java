package com.hometech.hometech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reviewID;
    private int ratingValue;
    private Date date;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "productID")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID")
    private Customer customer;
    private String comment;

    @OneToOne(mappedBy = "review", fetch = FetchType.LAZY)
    private Response response;

    @Lob
    private byte[] image;

}
