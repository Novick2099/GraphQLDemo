package com.microserviceslab.graphqldemo.service;

import com.microserviceslab.graphqldemo.model.Book;
import com.microserviceslab.graphqldemo.repository.BookRepository;
import graphql.schema.DataFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public DataFetcher<CompletableFuture<Book>> getBook() {
        //TODO Java Lambda
        return env -> {
            UUID bookId = env.getArgument("id");
            return bookRepository.getBook(bookId).toFuture();
        };
    }

    public DataFetcher<CompletableFuture<List<Book>>> getBooks(){
        return env -> bookRepository.getBooks().collectList().toFuture();
    }
/*
Se usa Completable Future porque la api graphql de java no soporta tipos reactivos como Mono y Flux
asi que hay que convertir esos tipos a CompletableFuture solo en caso de susbscripcion podemos usar  publisher Type
 */
    public DataFetcher<CompletableFuture<String>> createBook(){
        //regresa el dataFetcher enviroment ?
        return env -> { //De donde sale ese env?
          String name = env.getArgument("name");
          int pages = env.getArgument("pages");
          //ASi se transforma en completable future
          return bookRepository.createBook(new Book(name, pages)).map(Object::toString).toFuture();
        };
    }
}
