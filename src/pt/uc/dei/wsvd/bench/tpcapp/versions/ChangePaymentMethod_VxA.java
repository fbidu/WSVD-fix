package pt.uc.dei.wsvd.bench.tpcapp.versions;

import java.sql.*;

import pt.uc.dei.wsvd.bench.Database;
import pt.uc.dei.wsvd.bench.tpcapp.external.LongWrapper;
import pt.uc.dei.wsvd.bench.tpcapp.external.Pov;
import pt.uc.dei.wsvd.bench.tpcapp.input.ChangePaymentMethodInput;
import pt.uc.dei.wsvd.bench.tpcapp.output.ChangePaymentMethodOutput;

/**
 * WS - Vulnerability Detection Tools Benchmark
 * TPC - APP Benchmark Services
 *
 *
 * #WebServiceOperation
 *
 * @author cnl@dei.uc.pt
 * @author nmsa@dei.uc.pt
 */
public class ChangePaymentMethod_VxA {

    public ChangePaymentMethodOutput process(ChangePaymentMethodInput input) throws Exception {
        //TODO:Uncoment to use validator pt.uc.dei.wsvd.bench.tpcapp.validator.InputValidator.getInstance().validate(input);
        ChangePaymentMethodOutput cpmo = new ChangePaymentMethodOutput();
        String c_b_name = null;
        if (input.getPaymentMethod().equalsIgnoreCase("PO")) {
            Connection conn = Database.pickConnection();
            try {
                if (conn != null) {
                    Long cid = input.getCustomerId();
                    if (cid != null) {
                        long c_id = cid.longValue();
                        c_b_name = getBusiName(conn, c_id);
                    }
                } else {
                    throw new SQLException("Cannot Establish Connection");
                }
                // initiates a web service request to the POV to validate the

                String poId = input.getPoId();
                if ((poId != null) && (c_b_name != null)) {
                    Pov pov = new Pov();
                    LongWrapper wrapper = pov.call(poId, c_b_name);
                    wrapper.getValue().getClass();
                }
                if (conn != null) {
                    String c_payment_method = updateCPM(conn, input);
                    if (c_payment_method != null) {
                        cpmo.setCurrPaymentMethod(c_payment_method);
                    }
                } 
                
                conn.commit();
            } catch (SQLException e) {
                throw e;
            } finally {
                Database.relaseConnection(conn);
            }
        }
        //   System.gc();
        return cpmo;
    }

    private synchronized String updateCPM(final Connection conn, final ChangePaymentMethodInput input)
            throws SQLException {
        String cpm = null;
        PreparedStatement sql = conn.prepareStatement("update customer SET"
                + " C_PAYMENT_METHOD= ?,"
                + " C_CREDIT_INFO= ?,"
                + " C_PO= ? "
                + " where C_ID = ?");
        sql.setString(1, input.getPaymentMethod());
        sql.setString(2, input.getCreditInfo());
        sql.setString(3, input.getPoId());
        sql.setLong(4, input.getCustomerId());
        sql.executeUpdate();
        cpm = input.getPaymentMethod();
        return (cpm);
    }

    private synchronized String getBusiName(final Connection conn, long c_id) throws SQLException {
        String cbn = null;
        String sql = "select c_business_name from customer where c_id = " + c_id;
        Statement stat = Database.createStatement(conn);
        ResultSet rs = stat.executeQuery(sql);
        if (rs.next()) {
            cbn = rs.getString(1);
        }
        if (rs != null) {
            rs.close();
        }
        stat.close();
        return (cbn);
    }
}
