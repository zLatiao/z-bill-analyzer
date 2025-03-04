package com.zzz.account.dao;

import com.zzz.account.entity.*;
import com.zzz.account.parser.AlipayBillExcelParser;
import com.zzz.account.parser.CmbBillExcelParser;
import com.zzz.account.parser.IBillExcelParser;
import com.zzz.account.parser.WxBillExcelParser;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;

/**
 * @author z-latiao
 * @since 2025/2/27 10:54
 */
public class BillDao {
    public static void main(String[] args) throws SQLException, IOException {
        IBillExcelParser<WxBillInfo, WxBillRecord> wxBillParser = new WxBillExcelParser();
        WxBillInfo wxBillInfo = wxBillParser.parse(new File("C:\\Users\\anjun\\Desktop\\账单\\微信支付账单(20240216-20240516).csv"));
        System.out.println(wxBillInfo);

        IBillExcelParser<AlipayBillInfo, AlipayBillRecord> alipayBillParser = new AlipayBillExcelParser();
        AlipayBillInfo alipayBillInfo = alipayBillParser.parse(new File("C:\\Users\\anjun\\Desktop\\账单\\alipay_record_20250221_103151.csv"));
        System.out.println(alipayBillInfo);


        IBillExcelParser<CmbBillInfo, CmbBillRecord> cmbBillParser = new CmbBillExcelParser();
        CmbBillInfo cmbBillInfo = cmbBillParser.parse(new File("C:\\Users\\anjun\\Desktop\\账单\\CMB_6214--------9016_20230516_20240516.csv"));
        System.out.println(cmbBillInfo);

        List<Bill> list = List.of(wxBillInfo, alipayBillInfo, cmbBillInfo).stream().flatMap(x -> x.getBills().stream()).toList();
        new BillDao().batchInsertBills(list, list.size());
    }

    private static final String URL = "jdbc:mysql://localhost:3306/z-account";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    public void insertBill(Bill bill) throws SQLException {
        String sql = "INSERT INTO bill (amount, amount_type, transaction_type, source, time, " +
                "counterparty, product, payment_mode, transaction_status, bill_no, " +
                "merchant_no, remark, card_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, bill.getAmount());
            pstmt.setInt(2, bill.getAmountType());
            pstmt.setString(3, bill.getTransactionType());
            pstmt.setInt(4, bill.getSource());
            pstmt.setObject(5, bill.getTransactionTime()); // 处理LocalDateTime
            pstmt.setString(6, bill.getCounterparty());
            pstmt.setString(7, bill.getProduct());
            pstmt.setString(8, bill.getPaymentMode());
            pstmt.setString(9, bill.getTransactionStatus());
            pstmt.setString(10, bill.getBillNo());
            pstmt.setString(11, bill.getMerchantNo());
            pstmt.setString(12, bill.getRemark());
            pstmt.setString(13, bill.getBankAccountLast4Number());

            pstmt.executeUpdate();
        }
    }

    // 批量写入方法（支持事务）
    public void batchInsertBills(List<Bill> bills, int batchSize) throws SQLException {
        String sql = "INSERT INTO bill (amount, amount_type, transaction_type, source, time, " +
                "counterparty, product, payment_mode, transaction_status, bill_no, " +
                "merchant_no, remark, card_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // 关闭自动提交，开启事务

            int count = 0;
            for (Bill bill : bills) {
                pstmt.setBigDecimal(1, bill.getAmount());
                pstmt.setInt(2, bill.getAmountType());
                pstmt.setString(3, bill.getTransactionType());
                pstmt.setInt(4, bill.getSource());
                pstmt.setObject(5, bill.getTransactionTime());
                pstmt.setString(6, bill.getCounterparty());
                pstmt.setString(7, bill.getProduct());
                pstmt.setString(8, bill.getPaymentMode());
                pstmt.setString(9, bill.getTransactionStatus());
                pstmt.setString(10, bill.getBillNo());
                pstmt.setString(11, bill.getMerchantNo());
                pstmt.setString(12, bill.getRemark());
                pstmt.setString(13, bill.getBankAccountLast4Number());

                pstmt.addBatch(); // 添加到批处理
                count++;

                // 每 batchSize 条执行一次批处理
                if (count % batchSize == 0) {
                    pstmt.executeBatch();
                    conn.commit(); // 提交事务
                }
            }

            // 处理剩余数据
            int[] remainingCounts = pstmt.executeBatch();
            conn.commit(); // 最终提交

        } catch (BatchUpdateException e) {
            e.printStackTrace();
            // 可添加事务回滚逻辑（需保留Connection引用）
        }
    }
}