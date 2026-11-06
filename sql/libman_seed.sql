-- LIBMAN - Dữ liệu kiểm thử có thể chạy lại trên schema đã tạo.
USE libman;
SET NAMES utf8mb4;

INSERT INTO tblManager8 (managerId, fullName, username, passwordHash)
VALUES (1, 'Trần Thu Hà', 'manager.demo', SHA2('Manager@123', 256))
ON DUPLICATE KEY UPDATE fullName = VALUES(fullName), username = VALUES(username), passwordHash = VALUES(passwordHash);

INSERT INTO tblLibrarian8 (librarianId, fullName, username, passwordHash, phone, email)
VALUES
    (1, 'Nguyễn Văn An', 'librarian.demo', SHA2('Librarian@123', 256), '0901000001', 'an.nguyen@libman.local'),
    (2, 'Lê Thị Bình', 'librarian.binh', SHA2('Librarian@123', 256), '0901000002', 'binh.le@libman.local')
ON DUPLICATE KEY UPDATE fullName = VALUES(fullName), username = VALUES(username), passwordHash = VALUES(passwordHash),
    phone = VALUES(phone), email = VALUES(email);

INSERT INTO tblReader8 (readerId, fullName, dateOfBirth, address, phone, email, username, passwordHash)
VALUES (1, 'Phạm Minh Châu', '2004-05-18', 'Hà Nội', '0902000001', 'chau.pham@libman.local', 'reader.demo', SHA2('Reader@123', 256))
ON DUPLICATE KEY UPDATE fullName = VALUES(fullName), dateOfBirth = VALUES(dateOfBirth), address = VALUES(address),
    phone = VALUES(phone), email = VALUES(email), username = VALUES(username), passwordHash = VALUES(passwordHash);

INSERT INTO tblReaderCard8 (cardId, readerId, issueDate, expiryDate, status)
VALUES ('CARD-001', 1, '2026-01-01', '2027-01-01', 'ACTIVE')
ON DUPLICATE KEY UPDATE readerId = VALUES(readerId), issueDate = VALUES(issueDate), expiryDate = VALUES(expiryDate), status = VALUES(status);

INSERT INTO tblSupplier8 (supplierId, supplierName, address, phone, email)
VALUES
    (1, 'Công ty Phát hành Sách Alpha', 'Cầu Giấy, Hà Nội', '02437654321', 'contact@alpha.example'),
    (2, 'Nhà sách Alpha Việt Nam', 'Quận 3, TP. Hồ Chí Minh', '02839112468', 'sales@alphavn.example')
ON DUPLICATE KEY UPDATE supplierName = VALUES(supplierName), address = VALUES(address), phone = VALUES(phone), email = VALUES(email);

INSERT INTO tblDocument8 (documentId, title, category, author, publisher, publishYear)
VALUES
    ('S-001', 'Cấu trúc dữ liệu và giải thuật', 'Sách', 'Nguyễn Đức Nghĩa', 'NXB Đại học Quốc gia Hà Nội', 2023),
    ('GT-015', 'Cơ sở dữ liệu', 'Giáo trình', 'Nguyễn Văn Ba', 'NXB Đại học Quốc gia Hà Nội', 2022),
    ('TC-028', 'Tạp chí Khoa học số 09', 'Tạp chí', 'Ban biên tập', 'NXB Khoa học', 2026),
    ('GT-016', 'Lập trình Java Web', 'Giáo trình', 'Dau Hoang', 'NXB Giáo dục', 2025)
ON DUPLICATE KEY UPDATE title = VALUES(title), category = VALUES(category), author = VALUES(author),
    publisher = VALUES(publisher), publishYear = VALUES(publishYear);

INSERT INTO tblImportReceipt8 (importId, supplierId, librarianId, importDate, status, submissionToken)
VALUES
    (1, 1, 1, '2026-10-01', 'PAID', '11111111-1111-1111-1111-111111111111'),
    (2, 2, 2, '2026-10-05', 'UNPAID', '22222222-2222-2222-2222-222222222222')
ON DUPLICATE KEY UPDATE supplierId = VALUES(supplierId), librarianId = VALUES(librarianId), importDate = VALUES(importDate),
    status = VALUES(status), submissionToken = VALUES(submissionToken);

INSERT INTO tblImportDetail8 (importDetailId, importId, documentId, quantity, price)
VALUES
    (1, 1, 'S-001', 2, 120000.00),
    (2, 1, 'GT-015', 1, 95000.00),
    (3, 1, 'TC-028', 1, 70000.00),
    (4, 2, 'GT-016', 1, 88000.00)
ON DUPLICATE KEY UPDATE importId = VALUES(importId), documentId = VALUES(documentId), quantity = VALUES(quantity), price = VALUES(price);

INSERT INTO tblDocumentCopy8 (copyId, documentId, importDetailId, status)
VALUES
    ('S-001-C001', 'S-001', 1, 'AVAILABLE'),
    ('S-001-C002', 'S-001', 1, 'AVAILABLE'),
    ('GT-015-C001', 'GT-015', 2, 'LOANED'),
    ('TC-028-C001', 'TC-028', 3, 'AVAILABLE'),
    ('GT-016-C001', 'GT-016', 4, 'AVAILABLE')
ON DUPLICATE KEY UPDATE documentId = VALUES(documentId), importDetailId = VALUES(importDetailId), status = VALUES(status);

INSERT INTO tblInvoice8 (invoiceId, importId, invoiceDate)
VALUES
    (1, 1, '2026-10-01'),
    (2, 2, '2026-10-05')
ON DUPLICATE KEY UPDATE importId = VALUES(importId), invoiceDate = VALUES(invoiceDate);

INSERT INTO tblPayment8 (paymentId, invoiceId, paymentDate, amount, paymentMethod, paymentStatus)
VALUES
    (1, 1, '2026-10-01', 405000.00, 'BANK_TRANSFER', 'SUCCESS'),
    (2, 2, '2026-10-05', 88000.00, 'CASH', 'FAILED')
ON DUPLICATE KEY UPDATE invoiceId = VALUES(invoiceId), paymentDate = VALUES(paymentDate), amount = VALUES(amount),
    paymentMethod = VALUES(paymentMethod), paymentStatus = VALUES(paymentStatus);

INSERT INTO tblLoanReceipt8 (loanId, readerId, librarianId, loanDate, dueDate)
VALUES
    (1, 1, 1, '2026-10-10', '2026-10-24'),
    (2, 1, 1, '2026-09-01', '2026-09-15')
ON DUPLICATE KEY UPDATE readerId = VALUES(readerId), librarianId = VALUES(librarianId), loanDate = VALUES(loanDate), dueDate = VALUES(dueDate);

INSERT INTO tblLoanDetail8 (loanDetailId, loanId, copyId, returnStatus)
VALUES
    (1, 1, 'GT-015-C001', 'BORROWED'),
    (2, 2, 'S-001-C001', 'RETURNED')
ON DUPLICATE KEY UPDATE loanId = VALUES(loanId), copyId = VALUES(copyId), returnStatus = VALUES(returnStatus);

INSERT INTO tblReturnReceipt8 (returnId, readerId, librarianId, returnDate)
VALUES (1, 1, 1, '2026-09-10')
ON DUPLICATE KEY UPDATE readerId = VALUES(readerId), librarianId = VALUES(librarianId), returnDate = VALUES(returnDate);

INSERT INTO tblReturnDetail8 (returnDetailId, returnId, loanDetailId, conditionWhenReturned)
VALUES (1, 1, 2, 'GOOD')
ON DUPLICATE KEY UPDATE returnId = VALUES(returnId), loanDetailId = VALUES(loanDetailId), conditionWhenReturned = VALUES(conditionWhenReturned);
