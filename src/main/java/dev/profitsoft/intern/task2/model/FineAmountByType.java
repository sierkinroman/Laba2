package dev.profitsoft.intern.task2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class FineAmountByType {

    @JacksonXmlProperty(isAttribute = true)
    private FineType type;

    @JacksonXmlProperty(localName = "fines_amount", isAttribute = true)
    private BigDecimal fineAmount;

}
