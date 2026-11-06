<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chi tiết tài liệu - LIBMAN</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<c:url var="searchRootUrl" value="/search-document-8" />
<nav class="navbar navbar-expand-lg navbar-dark bg-primary" aria-label="Điều hướng chính">
    <div class="container">
        <a class="navbar-brand fw-semibold" href="${searchRootUrl}">LIBMAN</a>
        <span class="navbar-text text-white">Tra cứu tài liệu · Reader</span>
    </div>
</nav>
<main class="container py-4">
    <c:url var="backUrl" value="/search-document-8">
        <c:if test="${not empty returnKeyword}">
            <c:param name="keyword" value="${returnKeyword}"/>
        </c:if>
    </c:url>
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 class="h3 mb-1">Chi tiết tài liệu</h1>
            <p class="text-muted mb-0">Thông tin và các bản sao đang quản lý</p>
        </div>
        <a class="btn btn-outline-secondary" href="${backUrl}">Quay lại danh sách</a>
    </div>

    <section class="card shadow-sm mb-4" aria-labelledby="document-heading">
        <div class="card-body">
            <h2 id="document-heading" class="h4"><c:out value="${document.title}"/></h2>
            <dl class="row mb-0">
                <dt class="col-sm-3">Mã tài liệu</dt>
                <dd class="col-sm-9"><c:out value="${document.documentId}"/></dd>
                <dt class="col-sm-3">Loại</dt>
                <dd class="col-sm-9"><c:out value="${document.category}"/></dd>
                <dt class="col-sm-3">Tác giả</dt>
                <dd class="col-sm-9"><c:out value="${document.author}"/></dd>
                <dt class="col-sm-3">Nhà xuất bản</dt>
                <dd class="col-sm-9"><c:out value="${document.publisher}"/></dd>
                <dt class="col-sm-3">Năm xuất bản</dt>
                <dd class="col-sm-9"><c:out value="${document.publishYear}"/></dd>
            </dl>
        </div>
    </section>

    <section class="card shadow-sm" aria-labelledby="copies-heading">
        <div class="card-body">
            <h2 id="copies-heading" class="h5">Danh sách bản sao</h2>
            <c:choose>
                <c:when test="${empty copies}">
                    <div class="alert alert-info mb-0">Tài liệu này hiện chưa có bản sao.</div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                            <tr>
                                <th scope="col">Mã bản sao</th>
                                <th scope="col">Mã tài liệu</th>
                                <th scope="col">Trạng thái</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="copy" items="${copies}">
                                <tr>
                                    <td><c:out value="${copy.copyId}"/></td>
                                    <td><c:out value="${copy.documentId}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${copy.status == 'AVAILABLE'}"><span class="badge text-bg-success"><c:out value="${copy.status}"/></span></c:when>
                                            <c:when test="${copy.status == 'LOANED'}"><span class="badge text-bg-warning"><c:out value="${copy.status}"/></span></c:when>
                                            <c:otherwise><span class="badge text-bg-secondary"><c:out value="${copy.status}"/></span></c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>
</body>
</html>
