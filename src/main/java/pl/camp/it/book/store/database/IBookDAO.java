package pl.camp.it.book.store.database;

import pl.camp.it.book.store.model.Book;

import java.util.List;
import java.util.Optional;

public interface IBookDAO {
    List<Book> getAllBooks();
    void persistBook(Book book);
    Optional<Book> getBookById(int id);
    boolean deleteBook(int id);
    void updateBook(Book book);
}
