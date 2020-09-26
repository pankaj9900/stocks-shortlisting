package com.shortlisting.stocks.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Stock {
    private String name;
    private double holding;
    private int count;
}
