<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chọn nhà cung cấp - LIBMAN</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-dark bg-primary mb-4">
    <div class="container"><a class="navbar-brand" href="${pageContext.request.contextPath}/import-document-8">LIBMAN</a>
        <span class="navbar-text text-white">Nhập tài liệu</span></div>
</nav>
<main class="container pb-5">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1 class="h3 mb-0">Tìm nhà cung cấp</h1>
        <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/import-document-8?action=add-supplier">Thêm nhà cung cấp</a>
    </div>
    <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
    <c:if test="${not empty success}"><div class="alert alert-success"><c:out value="${success}"/></div></c:if>
    <c:if test="${not empty selectedSupplier}">
        <div class="alert alert-success">
            Đã chọn: <strong><c:out value="${selectedSupplier.supplierName}"/></strong>.
            Hệ thống sử dụng thủ thư demo mã 1 cho luồng chưa tích hợp đăng nhập.
            <form method="post" action="${pageContext.request.contextPath}/import-document-8" class="row g-2 mt-2">
                <input type="hidden" name="action" value="open-receipt">
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="supplierId" value="<c:out value='${selectedSupplier.supplierId}'/>">
                <div class="col-auto"><span class="form-control-plaintext">Thủ thư demo: mã 1</span></div>
                <div class="col-auto"><button class="btn btn-success" type="submit">Bắt đầu phiếu nhập</button></div>
            </form>
        </div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/import-document-8" class="card card-body shadow-sm mb-4">
        <input type="hidden" name="action" value="search-supplier">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
        <label for="keyword" class="form-label">Tên nhà cung cấp</label>
        <div class="input-group"><input id="keyword" name="keyword" class="form-control" value="<c:out value='${keyword}'/>" maxlength="200" required>
            <button class="btn btn-primary" type="submit">Tìm kiếm</button></div>
    </form>
    <c:choose>
        <c:when test="${not empty suppliers}">
            <div class="table-responsive"><table class="table table-bordered table-hover bg-white align-middle">
                <thead class="table-light"><tr><th>Mã</th><th>Tên nhà cung cấp</th><th>Địa chỉ</th><th>Điện thoại</th><th>Email</th><th></th></tr></thead>
                <tbody><c:forEach var="supplier" items="${suppliers}"><tr>
                    <td><c:out value="${supplier.supplierId}"/></td><td><c:out value="${supplier.supplierName}"/></td>
                    <td><c:out value="${supplier.address}"/></td><td><c:out value="${supplier.phone}"/></td><td><c:out value="${supplier.email}"/></td>
                    <td><form method="post" action="${pageContext.request.contextPath}/import-document-8" class="m-0">
                        <input type="hidden" name="action" value="open-receipt"><input type="hidden" name="supplierId" value="<c:out value='${supplier.supplierId}'/>">
                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>"><button class="btn btn-sm btn-success" type="submit">Chọn</button>
                    </form></td>
                </tr></c:forEach></tbody>
            </table></div>
        </c:when>
        <c:otherwise><c:choose><c:when test="${searchedSupplier}"><div class="alert alert-warning">Không tìm thấy nhà cung cấp phù hợp.</div></c:when><c:otherwise><div class="alert alert-secondary">Chưa có kết quả. Hãy nhập tên để tìm kiếm.</div></c:otherwise></c:choose></c:otherwise>
    </c:choose>
</main>
</body>
</html>
