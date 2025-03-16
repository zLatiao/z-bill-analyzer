//package com.z.billanalyzer.dao;
//
//import com.z.billanalyzer.domain.*;
//import com.z.billanalyzer.domain.parse.AlipayBillExcelParseResult;
//import com.z.billanalyzer.domain.parse.CmbBillExcelParseResult;
//import com.z.billanalyzer.domain.parse.WxBillExcelParseResult;
//import com.z.billanalyzer.parser.AlipayBillExcelParser;
//import com.z.billanalyzer.parser.CmbBillExcelParser;
//import com.z.billanalyzer.parser.IBillExcelParser;
//import com.z.billanalyzer.parser.WxBillExcelParser;
//
//import java.io.File;
//import java.io.IOException;
//import java.sql.*;
//import java.util.List;
//
///**
// * @author z-latiao
// * @since 2025/2/27 10:54
// */
//public class BillDao {
//    public static void main(String[] args) throws SQLException, IOException {
//        IBillExcelParser<WxBill, WxBillExcelParseResult> wxBillParser = new WxBillExcelParser();
//        WxBill wxBillInfo = wxBillParser.parse(new File("C:\\Users\\anjun\\Desktop\\账单\\微信支付账单(20240216-20240516).csv"));
//        System.out.println(wxBillInfo);
//
//        IBillExcelParser<AlipayBill, AlipayBillExcelParseResult> alipayBillParser = new AlipayBillExcelParser();
//        AlipayBill alipayBillInfo = alipayBillParser.parse(new File("C:\\Users\\anjun\\Desktop\\账单\\alipay_record_20250221_103151.csv"));
//        System.out.println(alipayBillInfo);
//
//
//        IBillExcelParser<CmbBill, CmbBillExcelParseResult> cmbBillParser = new CmbBillExcelParser();
//        CmbBill cmbBillInfo = cmbBillParser.parse(new File("C:\\Users\\anjun\\Desktop\\账单\\CMB_6214--------9016_20230516_20240516.csv"));
//        System.out.println(cmbBillInfo);
//
//        List<? extends BaseBillDetail> list = List.of(wxBillInfo, alipayBillInfo, cmbBillInfo).stream().flatMap(x -> x.getBillDetails().stream()).toList();
//        new BillDao().batchInsertBills(list, list.size());
//    }
//
//    private static final String URL = "jdbc:mysql://localhost:3306/z-account";
//    private static final String USER = "root";
//    private static final String PASSWORD = "123456";
//
//    public void insertBill(BillDetail billDetail) throws SQLException {
//        String sql = "INSERT INTO bill (amount, amount_type, transaction_type, source, time, " +
//                "counterparty, product, payment_mode, transaction_status, bill_no, " +
//                "merchant_no, remark, card_number) " +
//                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//
//        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setBigDecimal(1, billDetail.getAmount());
//            pstmt.setInt(2, billDetail.getAmountType());
//            pstmt.setString(3, billDetail.getTransactionType());
//            pstmt.setInt(4, billDetail.getSource());
//            pstmt.setObject(5, billDetail.getTransactionTime()); // 处理LocalDateTime
//            pstmt.setString(6, billDetail.getCounterparty());
//            pstmt.setString(7, billDetail.getProduct());
//            pstmt.setString(8, billDetail.getPaymentMode());
//            pstmt.setString(9, billDetail.getTransactionStatus());
//            pstmt.setString(10, billDetail.getBillNo());
//            pstmt.setString(11, billDetail.getMerchantNo());
//            pstmt.setString(12, billDetail.getRemark());
//            pstmt.setString(13, billDetail.getBankAccountLast4Number());
//
//            pstmt.executeUpdate();
//        }
//    }
//
//    // 批量写入方法（支持事务）
//    public void batchInsertBills(List<? extends BaseBillDetail> billDetails, int batchSize) throws SQLException {
//        String sql = "INSERT INTO bill (amount, amount_type, transaction_type, source, time, " +
//                "counterparty, product, payment_mode, transaction_status, bill_no, " +
//                "merchant_no, remark, card_number) " +
//                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//
//        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            conn.setAutoCommit(false); // 关闭自动提交，开启事务
//
//            int count = 0;
//            for (BaseBillDetail billDetail : billDetails) {
//                pstmt.setBigDecimal(1, billDetail.getAmount());
//                pstmt.setInt(2, billDetail.getAmountType());
//                pstmt.setString(3, billDetail.getTransactionType());
//                pstmt.setInt(4, billDetail.getSource());
//                pstmt.setObject(5, billDetail.getTransactionTime());
//                pstmt.setString(6, billDetail.getCounterparty());
////                pstmt.setString(7, billDetail.getProduct());
////                pstmt.setString(8, billDetail.getPaymentMode());
////                pstmt.setString(9, billDetail.getTransactionStatus());
////                pstmt.setString(10, billDetail.getBillNo());
////                pstmt.setString(11, billDetail.getMerchantNo());
//                pstmt.setString(12, billDetail.getRemark());
//                pstmt.setString(13, billDetail.getBankAccountLast4Number());
//
//                pstmt.addBatch(); // 添加到批处理
//                count++;
//
//                // 每 batchSize 条执行一次批处理
//                if (count % batchSize == 0) {
//                    pstmt.executeBatch();
//                    conn.commit(); // 提交事务
//                }
//            }
//
//            // 处理剩余数据
//            int[] remainingCounts = pstmt.executeBatch();
//            conn.commit(); // 最终提交
//
//        } catch (BatchUpdateException e) {
//            e.printStackTrace();
//            // 可添加事务回滚逻辑（需保留Connection引用）
//        }
//    }
//}