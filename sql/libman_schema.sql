-- LIBMAN - MySQL 8 schema
-- Chạy bằng một tài khoản MySQL có quyền CREATE DATABASE và CREATE TABLE.

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS libman
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_vi_0900_ai_ci;

USE libman;

CREATE TABLE IF NOT EXISTS tblReader8 (
    readerId BIGINT PRIMARY KEY AUTO_INCREMENT,
    fullName VARCHAR(150) NOT NULL,
    dateOfBirth DATE NOT NULL,
    address TEXT NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    username VARCHAR(80) NOT NULL,
    passwordHash VARCHAR(255) NOT NULL,
    CONSTRAINT uq_reader8_username UNIQUE (username),
    CONSTRAINT uq_reader8_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblLibrarian8 (
    librarianId BIGINT PRIMARY KEY AUTO_INCREMENT,
    fullName VARCHAR(150) NOT NULL,
    username VARCHAR(80) NOT NULL,
    passwordHash VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    CONSTRAINT uq_librarian8_username UNIQUE (username),
    CONSTRAINT uq_librarian8_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblManager8 (
    managerId BIGINT PRIMARY KEY AUTO_INCREMENT,
    fullName VARCHAR(150) NOT NULL,
    username VARCHAR(80) NOT NULL,
    passwordHash VARCHAR(255) NOT NULL,
    CONSTRAINT uq_manager8_username UNIQUE (username)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblSupplier8 (
    supplierId BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplierName VARCHAR(200) NOT NULL,
    address TEXT NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    INDEX idx_supplier8_name (supplierName)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblDocument8 (
    documentId VARCHAR(30) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(80) NOT NULL,
    author VARCHAR(150) NOT NULL,
    publisher VARCHAR(150) NOT NULL,
    publishYear INT NOT NULL,
    CONSTRAINT chk_document8_publish_year CHECK (publishYear BETWEEN 1000 AND 9999),
    INDEX idx_document8_title (title)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblReaderCard8 (
    cardId VARCHAR(30) PRIMARY KEY,
    readerId BIGINT NOT NULL,
    issueDate DATE NOT NULL,
    expiryDate DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT chk_reader_card8_dates CHECK (expiryDate >= issueDate),
    CONSTRAINT uq_reader_card8_reader UNIQUE (readerId),
    CONSTRAINT fk_reader_card8_reader FOREIGN KEY (readerId)
        REFERENCES tblReader8 (readerId) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblImportReceipt8 (
    importId BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplierId BIGINT NOT NULL,
    librarianId BIGINT NOT NULL,
    importDate DATE NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'UNPAID',
    submissionToken CHAR(36) NOT NULL,
    CONSTRAINT chk_import_receipt8_status CHECK (status IN ('UNPAID', 'PAID')),
    CONSTRAINT uq_import_receipt8_submission_token UNIQUE (submissionToken),
    CONSTRAINT fk_import_receipt8_supplier FOREIGN KEY (supplierId)
        REFERENCES tblSupplier8 (supplierId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_import_receipt8_librarian FOREIGN KEY (librarianId)
        REFERENCES tblLibrarian8 (librarianId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_import_receipt8_supplier_date (supplierId, importDate),
    INDEX idx_import_receipt8_librarian_date (librarianId, importDate)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblImportDetail8 (
    importDetailId BIGINT PRIMARY KEY AUTO_INCREMENT,
    importId BIGINT NOT NULL,
    documentId VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    CONSTRAINT chk_import_detail8_quantity CHECK (quantity > 0),
    CONSTRAINT chk_import_detail8_price CHECK (price >= 0),
    CONSTRAINT fk_import_detail8_receipt FOREIGN KEY (importId)
        REFERENCES tblImportReceipt8 (importId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_import_detail8_document FOREIGN KEY (documentId)
        REFERENCES tblDocument8 (documentId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_import_detail8_import (importId),
    INDEX idx_import_detail8_document (documentId)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblDocumentCopy8 (
    copyId VARCHAR(50) PRIMARY KEY,
    documentId VARCHAR(30) NOT NULL,
    importDetailId BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT chk_document_copy8_status CHECK (status IN ('AVAILABLE', 'LOANED', 'LOST', 'DAMAGED')),
    CONSTRAINT fk_document_copy8_document FOREIGN KEY (documentId)
        REFERENCES tblDocument8 (documentId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_document_copy8_import_detail FOREIGN KEY (importDetailId)
        REFERENCES tblImportDetail8 (importDetailId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_document_copy8_document_status (documentId, status),
    INDEX idx_document_copy8_import_detail (importDetailId)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblInvoice8 (
    invoiceId BIGINT PRIMARY KEY AUTO_INCREMENT,
    importId BIGINT NOT NULL,
    invoiceDate DATE NOT NULL,
    CONSTRAINT uq_invoice8_import UNIQUE (importId),
    CONSTRAINT fk_invoice8_import FOREIGN KEY (importId)
        REFERENCES tblImportReceipt8 (importId) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblPayment8 (
    paymentId BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoiceId BIGINT NOT NULL,
    paymentDate DATE NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    paymentMethod VARCHAR(30) NOT NULL,
    paymentStatus VARCHAR(30) NOT NULL,
    successfulInvoiceId BIGINT GENERATED ALWAYS AS
        (CASE WHEN paymentStatus = 'SUCCESS' THEN invoiceId ELSE NULL END) STORED,
    CONSTRAINT chk_payment8_amount CHECK (amount >= 0),
    CONSTRAINT chk_payment8_method CHECK (paymentMethod IN ('CASH', 'BANK_TRANSFER')),
    CONSTRAINT chk_payment8_status CHECK (paymentStatus IN ('SUCCESS', 'FAILED')),
    CONSTRAINT uq_payment8_successful_invoice UNIQUE (successfulInvoiceId),
    CONSTRAINT fk_payment8_invoice FOREIGN KEY (invoiceId)
        REFERENCES tblInvoice8 (invoiceId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_payment8_invoice_status (invoiceId, paymentStatus)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblLoanReceipt8 (
    loanId BIGINT PRIMARY KEY AUTO_INCREMENT,
    readerId BIGINT NOT NULL,
    librarianId BIGINT NOT NULL,
    loanDate DATE NOT NULL,
    dueDate DATE NOT NULL,
    CONSTRAINT chk_loan_receipt8_dates CHECK (dueDate >= loanDate),
    CONSTRAINT fk_loan_receipt8_reader FOREIGN KEY (readerId)
        REFERENCES tblReader8 (readerId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_loan_receipt8_librarian FOREIGN KEY (librarianId)
        REFERENCES tblLibrarian8 (librarianId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_loan_receipt8_reader (readerId),
    INDEX idx_loan_receipt8_librarian (librarianId)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblLoanDetail8 (
    loanDetailId BIGINT PRIMARY KEY AUTO_INCREMENT,
    loanId BIGINT NOT NULL,
    copyId VARCHAR(50) NOT NULL,
    returnStatus VARCHAR(30) NOT NULL,
    CONSTRAINT chk_loan_detail8_return_status CHECK (returnStatus IN ('BORROWED', 'RETURNED')),
    CONSTRAINT fk_loan_detail8_receipt FOREIGN KEY (loanId)
        REFERENCES tblLoanReceipt8 (loanId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_loan_detail8_copy FOREIGN KEY (copyId)
        REFERENCES tblDocumentCopy8 (copyId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_loan_detail8_loan (loanId),
    INDEX idx_loan_detail8_copy (copyId)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblReturnReceipt8 (
    returnId BIGINT PRIMARY KEY AUTO_INCREMENT,
    readerId BIGINT NOT NULL,
    librarianId BIGINT NOT NULL,
    returnDate DATE NOT NULL,
    CONSTRAINT fk_return_receipt8_reader FOREIGN KEY (readerId)
        REFERENCES tblReader8 (readerId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_return_receipt8_librarian FOREIGN KEY (librarianId)
        REFERENCES tblLibrarian8 (librarianId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_return_receipt8_reader (readerId),
    INDEX idx_return_receipt8_librarian (librarianId)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblReturnDetail8 (
    returnDetailId BIGINT PRIMARY KEY AUTO_INCREMENT,
    returnId BIGINT NOT NULL,
    loanDetailId BIGINT NOT NULL,
    conditionWhenReturned VARCHAR(100) NOT NULL,
    CONSTRAINT uq_return_detail8_loan_detail UNIQUE (loanDetailId),
    CONSTRAINT fk_return_detail8_receipt FOREIGN KEY (returnId)
        REFERENCES tblReturnReceipt8 (returnId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_return_detail8_loan_detail FOREIGN KEY (loanDetailId)
        REFERENCES tblLoanDetail8 (loanDetailId) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_return_detail8_return (returnId)
) ENGINE=InnoDB;
