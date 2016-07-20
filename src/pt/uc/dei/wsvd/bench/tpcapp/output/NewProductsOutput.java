package pt.uc.dei.wsvd.bench.tpcapp.output;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * WS - Vulnerability Detection Tools Benchmark
 * TPC - APP Benchmark Services
 *
 *
 *
 *
 * @author cnl@dei.uc.pt
 * @author nmsa@dei.uc.pt
 */
@XmlType(name = "NewProductsOutput", namespace = "http://tpcapp.bench.wsvd.dei.uc.pt/")
public class NewProductsOutput {

    List<NewProduct> newProductsList;

    public NewProductsOutput() {
        super();
    }

    @XmlElement(namespace = "http://tpcapp.bench.wsvd.dei.uc.pt/", type = NewProduct.class)
    public List<NewProduct> getNewProductsList() {
        return newProductsList;
    }

    public void setNewProductsList(List<NewProduct> newProductsList) {
        this.newProductsList = newProductsList;
    }
}
