<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thêm nhà cung cấp - LIBMAN</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
<body class="bg-light"><nav class="navbar navbar-dark bg-primary mb-4"><div class="container"><a class="navbar-brand" href="${pageContext.request.contextPath}/import-document-8">LIBMAN</a><span class="navbar-text text-white">Nhập tài liệu</span></div></nav>
<main class="container pb-5"><div class="row justify-content-center"><div class="col-lg-8"><div class="card shadow-sm"><div class="card-body">
    <h1 class="h3 mb-3">Thêm nhà cung cấp</h1>
    <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
    <form method="post" action="${pageContext.request.contextPath}/import-document-8">
        <input type="hidden" name="action" value="add-supplier">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
        <div class="mb-3"><label for="supplierName" class="form-label">Tên nhà cung cấp</label><input class="form-control" id="supplierName" name="supplierName" maxlength="200" value="<c:out value='${param.supplierName}'/>" required></div>
        <div class="mb-3"><label for="address" class="form-label">Địa chỉ</label><textarea class="form-control" id="address" name="address" rows="2" required><c:out value="${param.address}"/></textarea></div>
        <div class="row"><div class="col-md-6 mb-3"><label for="phone" class="form-label">Điện thoại</label><input class="form-control" id="phone" name="phone" maxlength="20" value="<c:out value='${param.phone}'/>" required></div>
            <div class="col-md-6 mb-3"><label for="email" class="form-label">Email</label><input type="email" class="form-control" id="email" name="email" maxlength="255" value="<c:out value='${param.email}'/>" required></div></div>
        <button class="btn btn-primary" type="submit">Lưu nhà cung cấp</button>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/import-document-8">Hủy</a>
    </form>
</div></div></div></div></main></body></html>
