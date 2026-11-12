package libman.web;

import libman.dao.DocumentCopyDAO8;
import libman.dao.DocumentDAO8;
import libman.model.DocumentCopy8;
import libman.model.Document8;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/search-document-8", "/documents"})
public class SearchDocumentController8 extends HttpServlet {
    private static final String SEARCH_VIEW = "/WEB-INF/jsp/SearchDocumentUI8.jsp";
    private static final String DETAIL_VIEW = "/WEB-INF/jsp/DocumentDetailUI8.jsp";
    private static final String DOCUMENT_ID_PATTERN = "[A-Za-z0-9][A-Za-z0-9_-]{0,29}";

    private final DocumentDAO8 documentDAO8 = new DocumentDAO8();
    private final DocumentCopyDAO8 documentCopyDAO8 = new DocumentCopyDAO8();

    public List<Document8> searchByTitle(String keyword) throws SQLException {
        return documentDAO8.searchByTitle(keyword);
    }

    public Optional<Document8> getDetail(String documentId) throws SQLException {
        return documentDAO8.findById(documentId);
    }

    public List<DocumentCopy8> getCopies(String documentId) throws SQLException {
        return documentCopyDAO8.findByDocumentId(documentId);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String rawDocumentId = request.getParameter("documentId");
        String documentId = rawDocumentId == null ? null : rawDocumentId.trim();
        String rawKeyword = request.getParameter("keyword");
        String keyword = rawKeyword == null ? null : rawKeyword.trim();

        if (rawDocumentId != null) {
            showDetail(request, response, documentId, keyword);
            return;
        }

        showSearchResults(request, response, rawKeyword, keyword);
    }

    private void showSearchResults(HttpServletRequest request, HttpServletResponse response,
                                   String rawKeyword, String keyword)
            throws ServletException, IOException {
        prepareSearchView(request, keyword);

        if (rawKeyword == null) {
            forward(request, response, SEARCH_VIEW);
            return;
        }
        if (keyword == null || keyword.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập toàn bộ hoặc một phần tên tài liệu.");
            forward(request, response, SEARCH_VIEW);
            return;
        }
        if (keyword.length() > 255) {
            request.setAttribute("error", "Từ khóa tìm kiếm không được vượt quá 255 ký tự.");
            forward(request, response, SEARCH_VIEW);
            return;
        }

        try {
            request.setAttribute("documents", searchByTitle(keyword));
            request.setAttribute("searched", true);
            forward(request, response, SEARCH_VIEW);
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            forward(request, response, SEARCH_VIEW);
        } catch (SQLException exception) {
            log("Không thể tìm kiếm tài liệu.", exception);
            request.setAttribute("error", "Không thể truy vấn cơ sở dữ liệu. Vui lòng thử lại sau.");
            forward(request, response, SEARCH_VIEW);
        }
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response,
                            String documentId, String returnKeyword)
            throws ServletException, IOException {
        String safeReturnKeyword = returnKeyword == null ? "" : returnKeyword;
        request.setAttribute("returnKeyword", safeReturnKeyword);
        if (documentId == null || !documentId.matches(DOCUMENT_ID_PATTERN)) {
            prepareSearchView(request, safeReturnKeyword);
            request.setAttribute("error", "Mã tài liệu không hợp lệ.");
            forward(request, response, SEARCH_VIEW);
            return;
        }

        try {
            Optional<Document8> document = getDetail(documentId);
            if (document.isEmpty()) {
                restoreSearchResults(request, safeReturnKeyword);
                request.setAttribute("error", "Không tìm thấy tài liệu với mã đã chọn. Tài liệu có thể đã bị xóa.");
                forward(request, response, SEARCH_VIEW);
                return;
            }
            request.setAttribute("document", document.get());
            request.setAttribute("copies", getCopies(documentId));
            forward(request, response, DETAIL_VIEW);
        } catch (IllegalArgumentException exception) {
            prepareSearchView(request, safeReturnKeyword);
            request.setAttribute("error", exception.getMessage());
            forward(request, response, SEARCH_VIEW);
        } catch (SQLException exception) {
            log("Không thể tải chi tiết tài liệu.", exception);
            prepareSearchView(request, safeReturnKeyword);
            request.setAttribute("error", "Không thể truy vấn cơ sở dữ liệu. Vui lòng thử lại sau.");
            forward(request, response, SEARCH_VIEW);
        }
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(view);
        dispatcher.forward(request, response);
    }

    private void prepareSearchView(HttpServletRequest request, String keyword) {
        request.setAttribute("documents", Collections.emptyList());
        request.setAttribute("searched", false);
        request.setAttribute("keyword", keyword == null ? "" : keyword);
    }

    private void restoreSearchResults(HttpServletRequest request, String keyword) throws SQLException {
        prepareSearchView(request, keyword);
        if (keyword != null && !keyword.isBlank() && keyword.length() <= 255) {
            request.setAttribute("documents", searchByTitle(keyword));
            request.setAttribute("searched", true);
        }
    }
}
