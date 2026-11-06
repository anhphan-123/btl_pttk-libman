<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Thêm tài liệu - LIBMAN</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
<body class="bg-light"><nav class="navbar navbar-dark bg-primary mb-4"><div class="container"><a class="navbar-brand" href="${pageContext.request.contextPath}/import-document-8">LIBMAN</a><span class="navbar-text text-white">Nhập tài liệu</span></div></nav>
<main class="container pb-5"><div class="row justify-content-center"><div class="col-lg-8"><div class="card shadow-sm"><div class="card-body"><h1 class="h3 mb-3">Thêm tài liệu mới</h1><c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
    <form method="post" action="${pageContext.request.contextPath}/import-document-8"><input type="hidden" name="action" value="add-document"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
        <div class="mb-3"><label class="form-label" for="documentId">Mã tài liệu</label><input class="form-control" id="documentId" name="documentId" maxlength="30" value="<c:out value='${documentId}'/>" required></div>
        <div class="mb-3"><label class="form-label" for="title">Tên tài liệu</label><input class="form-control" id="title" name="title" maxlength="255" value="<c:out value='${param.title}'/>" required></div>
        <div class="row"><div class="col-md-6 mb-3"><label class="form-label" for="category">Thể loại</label><input class="form-control" id="category" name="category" maxlength="80" value="<c:out value='${param.category}'/>" required></div><div class="col-md-6 mb-3"><label class="form-label" for="publishYear">Năm xuất bản</label><input class="form-control" id="publishYear" name="publishYear" type="number" min="1000" max="9999" value="<c:out value='${param.publishYear}'/>" required></div></div>
        <div class="mb-3"><label class="form-label" for="author">Tác giả</label><input class="form-control" id="author" name="author" maxlength="150" value="<c:out value='${param.author}'/>" required></div>
        <div class="mb-3"><label class="form-label" for="publisher">Nhà xuất bản</label><input class="form-control" id="publisher" name="publisher" maxlength="150" value="<c:out value='${param.publisher}'/>" required></div>
        <button class="btn btn-primary" type="submit">Lưu tài liệu</button><a class="btn btn-secondary" href="${pageContext.request.contextPath}/import-document-8?action=receipt">Hủy</a>
    </form></div></div></div></div></main></body></html>
