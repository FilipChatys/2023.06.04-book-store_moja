package pl.camp.it.book.store.services.impl;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.camp.it.book.store.database.IBookDAO;
import pl.camp.it.book.store.database.IOrderDAO;
import pl.camp.it.book.store.model.Book;
import pl.camp.it.book.store.model.Cart;
import pl.camp.it.book.store.model.Order;
import pl.camp.it.book.store.model.OrderPosition;
import pl.camp.it.book.store.services.ICartService;
import pl.camp.it.book.store.session.SessionData;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    IBookDAO bookDAO;

    @Autowired
    IOrderDAO orderDAO;

    @Resource
    SessionData sessionData;

    @Override
    public void addProductToCart(int id) {
        Optional<Book> bookBox = this.bookDAO.getBookById(id);
        if(bookBox.isEmpty()) {
            return;
        }
        Book book = bookBox.get();
        Cart cart = this.sessionData.getCart();
        for(OrderPosition orderPosition : cart.getPositions()) {
            if(orderPosition.getBook().getId() == id) {
                if(orderPosition.getQuantity() < book.getQuantity()) {
                    orderPosition.incrementQuantity();
                }
                return;
            }
        }
        if(book.getQuantity() > 0) {
            OrderPosition orderPosition = new OrderPosition();
            orderPosition.setBook(book);
            orderPosition.setQuantity(1);
            cart.getPositions().add(orderPosition);
        }
    }

    @Override
    public void confirm() {
        Map<Book, Integer> booksToUpdateWithNewQuantity = new HashMap<>();
        boolean positionChanged = false;
        //TODO zamiana na stream
        for(OrderPosition orderPosition : this.sessionData.getCart().getPositions()) {
            Optional<Book> bookFromDbBox = this.bookDAO.getBookById(orderPosition.getBook().getId());
            if(bookFromDbBox.isEmpty()) {
                this.sessionData.getCart().getPositions().remove(orderPosition);
                return;
            }
            Book bookFromDb = bookFromDbBox.get();
            if(bookFromDb.getQuantity() < orderPosition.getQuantity()) {
                orderPosition.setQuantity(bookFromDb.getQuantity());
                positionChanged = true;
            }
            booksToUpdateWithNewQuantity.put(bookFromDb, bookFromDb.getQuantity() - orderPosition.getQuantity());
        }
        if(positionChanged) {
            return;
        }
        Order order = new Order();
        order.setUser(this.sessionData.getUser());
        order.getOrderPositions().addAll(this.sessionData.getCart().getPositions());
        order.setStatus(Order.Status.NEW);
        order.setTotal(this.calculateCartSum());
        order.setDateTime(LocalDateTime.now());
        this.orderDAO.persistOrder(order);
        for(Map.Entry<Book, Integer> entry : booksToUpdateWithNewQuantity.entrySet()) {
            entry.getKey().setQuantity(entry.getValue());
            this.bookDAO.updateBook(entry.getKey());
        }
        this.clearCart();
    }

    @Override
    public void removeFromCart(int id) {
        //TODO zamiana na stream
        Set<OrderPosition> orderPositions = this.sessionData.getCart().getPositions();
        for(OrderPosition orderPosition : orderPositions) {
            if(orderPosition.getBook().getId() == id) {
                orderPositions.remove(orderPosition);
                return;
            }
        }
    }

    @Override
    public void clearCart() {
        this.sessionData.getCart().getPositions().clear();
    }

    @Override
    public double calculateCartSum() {
        /*double sum = 0.0;
        for(OrderPosition orderPosition : this.sessionData.getCart().getPositions()) {
            sum = orderPosition.getQuantity() * orderPosition.getBook().getPrice() + sum;
        }
        return sum;*/
        return this.sessionData.getCart().getPositions().stream()
                .mapToDouble(op -> op.getQuantity() * op.getBook().getPrice())
                .sum();
    }
}
