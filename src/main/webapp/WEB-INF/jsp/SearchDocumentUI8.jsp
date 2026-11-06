<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tìm kiếm tài liệu - LIBMAN</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<c:url var="searchUrl" value="/search-document-8" />
<nav class="navbar navbar-expand-lg navbar-dark bg-primary" aria-label="Điều hướng chính">
    <div class="container">
        <a class="navbar-brand fw-semibold" href="${searchUrl}">LIBMAN</a>
        <span class="navbar-text text-white">Tra cứu tài liệu · Reader</span>
    </div>
</nav>
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 class="h3 mb-1">Tìm kiếm tài liệu</h1>
            <p class="text-muted mb-0">LIBMAN · Search Document 8</p>
        </div>
        <span class="badge text-bg-primary">Reader</span>
    </div>

    <section class="card shadow-sm mb-4" aria-labelledby="search-heading">
        <div class="card-body">
            <h2 id="search-heading" class="h5">Nhập tên tài liệu</h2>
            <form method="get" action="${searchUrl}" class="row g-2">
                <div class="col-md-9">
                    <label class="visually-hidden" for="keyword">Tên tài liệu</label>
                    <input id="keyword" name="keyword" type="search" class="form-control"
                           maxlength="255" placeholder="Ví dụ: Java Web"
                           value="<c:out value='${keyword}'/>" autofocus>
                </div>
                <div class="col-md-3 d-grid">
                    <button type="submit" class="btn btn-primary">Tìm kiếm</button>
                </div>
            </form>
        </div>
    </section>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${not empty info}">
        <div class="alert alert-info" role="status"><c:out value="${info}"/></div>
    </c:if>

    <c:if test="${searched and empty documents and empty error}">
        <div class="alert alert-warning" role="status">
            Không tìm thấy tài liệu phù hợp với từ khóa
            “<c:out value="${keyword}"/>”.
        </div>
    </c:if>

    <c:if test="${not empty documents}">
        <section class="card shadow-sm" aria-labelledby="result-heading">
            <div class="card-body">
                <h2 id="result-heading" class="h5">Kết quả tìm kiếm</h2>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                        <tr>
                            <th scope="col">Mã</th>
                            <th scope="col">Tên tài liệu</th>
                            <th scope="col">Loại</th>
                            <th scope="col">Tác giả</th>
                            <th scope="col">Nhà xuất bản</th>
                            <th scope="col" class="text-end">Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="document" items="${documents}">
                            <tr>
                                <td><c:out value="${document.documentId}"/></td>
                                <td><c:out value="${document.title}"/></td>
                                <td><c:out value="${document.category}"/></td>
                                <td><c:out value="${document.author}"/></td>
                                <td><c:out value="${document.publisher}"/></td>
                                <td class="text-end">
                                    <c:url var="detailUrl" value="/search-document-8">
                                        <c:param name="documentId" value="${document.documentId}"/>
                                        <c:param name="keyword" value="${keyword}"/>
                                    </c:url>
                                    <a class="btn btn-sm btn-outline-primary" href="${detailUrl}">Xem chi tiết</a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    </c:if>
</main>
</body>
</html>
