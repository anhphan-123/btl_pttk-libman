# LIBMAN

Hệ thống quản lý thư viện LIBMAN dùng Java 17, Maven, JSP/Servlet `javax.servlet` cho Tomcat 9, JDBC và MySQL 8. Search Document 8 và Import Document 8 đã được triển khai theo MVC.

## Nội dung Giai đoạn 2

- Entity JavaBeans có hậu tố `8` cho toàn bộ 15 bảng.
- DAO JDBC cho Search/Import: `DocumentDAO8`, `DocumentCopyDAO8`, `SupplierDAO8`, `LibrarianDAO8`, `ImportReceiptDAO8`, `ImportDetailDAO8`, `InvoiceDAO8`, `PaymentDAO8`.
- `ImportReceiptDAO8.saveReceiptWithDetailsAndInvoice(...)` dùng một transaction: phiếu nhập, chi tiết, bản sao và hóa đơn cùng thành công hoặc cùng rollback.
- `submissionToken` là unique key để một request Submit lặp lại không tạo phiếu mới.
- `tblInvoice8.importId` là unique key; generated unique column trong `tblPayment8` chặn hai payment `SUCCESS` cho cùng hóa đơn.
- Endpoint kỹ thuật `GET /libman/health` kiểm tra JDBC, không phải giao diện nghiệp vụ.

## Yêu cầu Linux Mint

```bash
sudo apt update
sudo apt install openjdk-17-jdk maven mysql-server curl
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
java -version
mvn -version
mysql --version
```

Nếu kho của Linux Mint có gói `tomcat9`, có thể cài thêm bằng `sudo apt install tomcat9`. Nếu không có (như môi trường kiểm thử), dùng binary Tomcat 9 chính thức:

```bash
cd /opt
sudo curl -LO https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.123/bin/apache-tomcat-9.0.123.tar.gz
sudo tar -xzf apache-tomcat-9.0.123.tar.gz
sudo mv apache-tomcat-9.0.123 tomcat9
sudo chown -R "$USER":"$USER" /opt/tomcat9
export CATALINA_HOME=/opt/tomcat9
export CATALINA_BASE=/opt/tomcat9
```

MySQL phải là 8.x. Khởi động dịch vụ nếu cần:

```bash
sudo systemctl enable --now mysql
```

## Tạo database và dữ liệu kiểm thử

Chạy hai script với tài khoản quản trị MySQL. Script schema không xóa dữ liệu hiện có; seed có thể chạy lại.

```bash
cd /duong-dan/toi/project
sudo mysql --default-character-set=utf8mb4 < sql/libman_schema.sql
sudo mysql --default-character-set=utf8mb4 < sql/libman_seed.sql
```

Tạo tài khoản riêng cho ứng dụng, không dùng tài khoản `root` trong Tomcat:

```sql
CREATE USER IF NOT EXISTS 'libman_app'@'localhost' IDENTIFIED BY 'MatKhauManhCuaBan';
GRANT SELECT, INSERT, UPDATE, DELETE ON libman.* TO 'libman_app'@'localhost';
FLUSH PRIVILEGES;
```

## Cấu hình kết nối không đưa vào Git

`DatabaseConfig8` chỉ đọc biến môi trường. Xem `.env.example`; không commit `.env` hay file cấu hình chứa mật khẩu thật. File `src/main/resources/db.properties.example` chỉ là tài liệu tham khảo và không được ứng dụng nạp.

Trong terminal dùng để build/test:

```bash
export LIBMAN_DB_HOST=127.0.0.1
export LIBMAN_DB_PORT=3306
export LIBMAN_DB_NAME=libman
export LIBMAN_DB_USER=libman_app
export LIBMAN_DB_PASSWORD='MatKhauManhCuaBan'
```

Cho Tomcat chạy bằng systemd, tạo `/etc/systemd/system/tomcat9.service.d/libman.conf`:

```ini
[Service]
Environment="LIBMAN_DB_HOST=127.0.0.1"
Environment="LIBMAN_DB_PORT=3306"
Environment="LIBMAN_DB_NAME=libman"
Environment="LIBMAN_DB_USER=libman_app"
Environment="LIBMAN_DB_PASSWORD=MatKhauManhCuaBan"
```

Sau đó chạy:

```bash
sudo systemctl daemon-reload
sudo systemctl restart tomcat9
```

## Build và kiểm thử

```bash
mvn clean verify
```

Nếu đã khai báo `LIBMAN_DB_USER` và `LIBMAN_DB_PASSWORD`, các test chỉ đọc xác minh 15 bảng, seed, tìm kiếm, tổng tiền, trạng thái và ràng buộc thanh toán. Nếu không có cấu hình, test JDBC được Maven báo `skipped` để build offline vẫn dùng được.

Các test Import có ghi dữ liệu chỉ chạy khi có cả hai điều kiện bảo vệ: tên database kết thúc bằng `_test` và `LIBMAN_RUN_MUTATING_DB_TESTS=true`. Không bật cờ này với database `libman` đang dùng thật.

```bash
export LIBMAN_DB_NAME=libman_test
export LIBMAN_RUN_MUTATING_DB_TESTS=true
mvn clean verify
```

Test ghi dữ liệu tự dọn đúng các dòng do chính test tạo; không reset hoặc xóa toàn bộ database.

Kiểm tra trực tiếp dữ liệu sau seed:

```sql
USE libman;
SELECT COUNT(*) AS total_tables
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name LIKE 'tbl%8';

SELECT ir.importId, ir.status, SUM(d.quantity * d.price) AS totalAmount
FROM tblImportReceipt8 ir
JOIN tblImportDetail8 d ON d.importId = ir.importId
GROUP BY ir.importId, ir.status;

SELECT invoiceId, COUNT(*) AS successfulPayments
FROM tblPayment8
WHERE paymentStatus = 'SUCCESS'
GROUP BY invoiceId
HAVING COUNT(*) > 1;
```

Kết quả mong đợi: `total_tables = 15`, tổng phiếu `importId = 1` là `405000.00`, và truy vấn cuối không trả dòng nào.

## Deploy Tomcat 9

```bash
mvn clean package
cp target/libman.war "$CATALINA_BASE/webapps/libman.war"
"$CATALINA_HOME/bin/catalina.sh" start
curl http://localhost:8080/libman/health
```

Trên môi trường terminal tự động dọn tiến trình nền, dùng `run` trong terminal riêng để giữ Tomcat hoạt động:

```bash
cd /duong-dan/toi/project
set -a; . ./.env; set +a
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export JRE_HOME="$JAVA_HOME"
export CATALINA_HOME=/opt/tomcat9
export CATALINA_BASE=/opt/tomcat9
"$CATALINA_HOME/bin/catalina.sh" run
```

Giữ terminal này mở trong lúc demo. Mật khẩu trong `.env` là mật khẩu riêng của `libman_app`, file có quyền `600` và không được commit.

Khi dùng gói Tomcat 9 do hệ điều hành quản lý, thay ba dòng đầu bằng `sudo cp target/libman.war /var/lib/tomcat9/webapps/libman.war` và `sudo systemctl restart tomcat9`.

Kết quả mong đợi khi database hoạt động:

```json
{"status":"UP","database":"libman"}
```

## Giai đoạn 3 - Search Document 8

Module tìm kiếm chỉ đọc dữ liệu từ MySQL và không làm thay đổi các bảng.

Sau khi deploy WAR, mở:

```text
http://localhost:8080/libman/search-document-8
```

Tìm theo toàn bộ hoặc một phần tên tài liệu. Chọn `Xem chi tiết` để xem thông tin tài liệu và các bản sao cùng trạng thái. Có thể dùng tham số `keyword` để giữ lại từ khóa khi quay về danh sách.

Controller là `SearchDocumentController8`; hai JSP nằm trong `WEB-INF/jsp`, nên không thể truy cập trực tiếp và không chứa SQL. Dữ liệu hiển thị dùng JSTL escaping để tránh XSS.

## Giai đoạn 4 - Import Document 8

Mở trang bắt đầu tại:

```text
http://localhost:8080/libman/import-document-8
```

Luồng demo: tìm/chọn nhà cung cấp (hoặc thêm mới) → dùng cố định thủ thư demo mã `1` ở phía server → tìm mã tài liệu (hoặc thêm mới) → nhập số lượng và đơn giá → sửa/xóa dòng → Submit. Draft chỉ nằm trong session; khi Submit, `ImportReceiptDAO8` dùng một JDBC transaction theo thứ tự phiếu → chi tiết → bản sao → hóa đơn. `submissionToken` và unique key ngăn Submit trùng, kể cả hai request đồng thời.

Mọi POST thay đổi dữ liệu đều cần CSRF token của session. Cố sửa `librarianId` hoặc đổi `supplierId` qua URL không làm thay đổi thủ thư/nhà cung cấp của draft.

Sau khi lưu, ứng dụng chuyển tới các URL:

- `/libman/import-document-8?action=invoice&invoiceId={id}`: xem/in hóa đơn bằng nút In của trình duyệt.
- `/libman/import-document-8?action=payment&invoiceId={id}`: ghi nhận tiền mặt/chuyển khoản, chọn `SUCCESS` hoặc `FAILED` để thử lại.

Chỉ một payment `SUCCESS` được chấp nhận cho mỗi hóa đơn; `FAILED` được lưu vào lịch sử và không đổi trạng thái phiếu. Số tiền `SUCCESS` phải đúng tổng `SUM(quantity * price)`. Sáu JSP của module nằm trong `WEB-INF/jsp`: `FindSupplierUI8.jsp`, `AddSupplierUI8.jsp`, `ImportReceiptUI8.jsp`, `AddDocumentUI8.jsp`, `InvoiceUI8.jsp`, `PaymentUI8.jsp`.
