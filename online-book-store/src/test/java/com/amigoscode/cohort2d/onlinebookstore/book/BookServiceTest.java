package com.amigoscode.cohort2d.onlinebookstore.book;

import com.amigoscode.cohort2d.onlinebookstore.author.Author;
import com.amigoscode.cohort2d.onlinebookstore.author.AuthorDTO;
import com.amigoscode.cohort2d.onlinebookstore.author.AuthorDTOMapper;
import com.amigoscode.cohort2d.onlinebookstore.category.Category;
import com.amigoscode.cohort2d.onlinebookstore.category.CategoryDTO;
import com.amigoscode.cohort2d.onlinebookstore.category.CategoryDTOMapper;
import com.amigoscode.cohort2d.onlinebookstore.exceptions.DuplicateResourceException;
import com.amigoscode.cohort2d.onlinebookstore.exceptions.RequestValidationException;
import com.amigoscode.cohort2d.onlinebookstore.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private BookService underTest;

    @Mock
    private BookDAO bookDAO;

    @BeforeEach
    void setUp() {
        underTest = new BookService(bookDAO);
    }

    @Test
    void shouldGetAllBooks() {

       // Given - more than one book
        Book request1 = BookDTOMapper.INSTANCE.dtoToModel(getBookDTO(1L, "12043953321", "The first Book"));

        Book request2 = BookDTOMapper.INSTANCE.dtoToModel(getBookDTO(2L, "12043953322", "The second book"));

        given(bookDAO.findAllBooks()).willReturn(List.of(request1, request2));

        List<BookDTO> expected = BookDTOMapper.INSTANCE.modelToDTO(bookDAO.findAllBooks());

        // When -  action or the behaviour that we are going test
        List<BookDTO> actual = underTest.getAllBooks();

        // Then - verify the output
        assertThat(actual).isNotNull();
        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    void shouldGetBookById() {

        // Given
        Long id = 1L;
        BookDTO request = getBookDTO(id, "12043953321", "The Lords of the Rings");

        given(bookDAO.findById(id)).willReturn(Optional.of(BookDTOMapper.INSTANCE.dtoToModel(request)));

        // When
        BookDTO actual = underTest.getBookById(id);

        // Then
        assertThat(actual).isEqualTo(request);

    }

    @Test
    void shouldAddBook() {

        // Given
        String isbn = "12043953324";
        BookDTO request = getBookDTO(null, isbn, null);
        given(bookDAO.existsBookByIsbn(isbn)).willReturn(false);

        // When
        underTest.addBook(request);

        // Then
        ArgumentCaptor<Book> bookArgumentCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookDAO).addBook(bookArgumentCaptor.capture());

        Book capturedBook = bookArgumentCaptor.getValue();

        assertThat(capturedBook.getId()).isNull();
        assertThat(capturedBook.getTitle()).isEqualTo(request.title());
        assertThat(capturedBook.getDescription()).isEqualTo(request.description());
        assertThat(capturedBook.getPrice()).isEqualTo(request.price());
        assertThat(capturedBook.getQuantity()).isEqualTo(request.quantity());
        assertThat(capturedBook.getNumberOfPages()).isEqualTo(request.numberOfPages());
        assertThat(capturedBook.getPublishDate()).isEqualTo(request.publishDate());
        assertThat(capturedBook.getBookFormat()).isEqualTo(request.bookFormat());

    }

    @Test
    void shouldDeleteBookById() {
        // Given
        Long id = 1L;
        given(bookDAO.existsBookById(id)).willReturn(true);

        // When
        underTest.deleteBookById(id);

        // Then
        verify(bookDAO).deleteBookById(id);
    }

    @Test
    void shouldThrowIfBookNotFoundWhenDeleting() {
        // Given
        Long id = 10L;

        // When && Then
        assertThatThrownBy(() -> underTest.deleteBookById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Book with id [%s] not found.".formatted(id));
        verify(bookDAO, never()).deleteBookById(id);
    }

void shouldUpdateBook() {
        // Given
        Long id = 10L;
        String isbn = "1234567891234";
        Author author = new Author(1L, "Douglas", "Norman");
        List<Author> authors = new ArrayList<>();
        authors.add(author);

        Category category = new Category(1L, "Best Sellers", "Mystery");
        List<Category> categories = new ArrayList<>();
        categories.add(category);

        //--- create new book
        Book book = new Book(
                id,
                isbn,
                "Lord of the Rings",
                "Fantasy book",
                BigDecimal.valueOf(19.99),
                300,
                250,
                LocalDate.of(1954, 7, 29),
                BookFormat.DIGITAL,
                authors,
                categories
        );

        given(bookDAO.findById(id)).willReturn(Optional.of(book));

        //--- set new details
        Author authorNew = new Author(2L, "Joan", "Doe");
        authors.add(authorNew);

        Category categoryNew = new Category(2L, "Horror", "Scary Books");
        categories.add(categoryNew);


        BookDTO bookDTORequest = new BookDTO(
                id,
                isbn,
                "The Hobbit",
                "The best book",
                BigDecimal.valueOf(29.99),
                150,
                300,
                LocalDate.of(1973, 7, 29),
                BookFormat.PHYSICAL,
                AuthorDTOMapper.INSTANCE.modelToDTO(authors),
                CategoryDTOMapper.INSTANCE.modelToDTO(categories)
        );

        // When
        underTest.updateBook(id, bookDTORequest);


        // Then
        ArgumentCaptor<Book> bookArgumentCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookDAO).updateBook(bookArgumentCaptor.capture());

        Book capturedBook = bookArgumentCaptor.getValue();

        assertThat(capturedBook.getId()).isEqualTo(bookDTORequest.id());
        assertThat(capturedBook.getIsbn()).isEqualTo(bookDTORequest.isbn());
        assertThat(capturedBook.getTitle()).isEqualTo(bookDTORequest.title());
        assertThat(capturedBook.getDescription()).isEqualTo(bookDTORequest.description());
        assertThat(capturedBook.getPrice()).isEqualTo(bookDTORequest.price());
        assertThat(capturedBook.getQuantity()).isEqualTo(bookDTORequest.quantity());
        assertThat(capturedBook.getNumberOfPages()).isEqualTo(bookDTORequest.numberOfPages());
        assertThat(capturedBook.getPublishDate()).isEqualTo(bookDTORequest.publishDate());
        assertThat(capturedBook.getBookFormat()).isEqualTo(bookDTORequest.bookFormat());
        assertThat(capturedBook.getAuthors()).isEqualTo(AuthorDTOMapper.INSTANCE.dtoToModel(bookDTORequest.authors()));
        assertThat(capturedBook.getCategories()).isEqualTo(CategoryDTOMapper.INSTANCE.dtoToModel(bookDTORequest.categories()));

    }

    @Test
    void shouldThrowWhenUpdateBookReturnEmptyOptional() {
        // Given
        Long id = 10L;
        given(bookDAO.findById(id)).willReturn(Optional.empty());

        BookDTO bookDTO = getBookDTO(10L, "1234567890123", "Hobbit");

        // When && Then
        assertThatThrownBy(() -> underTest.updateBook(id, bookDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Book with id [%s] not found.".formatted(id));
    }


    @Test
    void shouldThrowWhenGetBookReturnEmptyOptional() {
        // Given
        Long id = 10L;
        given(bookDAO.findById(id)).willReturn(Optional.empty());

        // When && Then
        assertThatThrownBy(() -> underTest.getBookById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Book with id [%s] not found.".formatted(id));
    }

    @Test
    void shouldThrowWhenAddingBookWithExistingIsbn() {
        // Given
        String isbn = "12043953321";
        BookDTO request = getBookDTO(1L, isbn, "Lord of the Rings");

        given(bookDAO.existsBookByIsbn(isbn)).willReturn(true);

        // When && Then
        assertThatThrownBy(() -> underTest.addBook(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Book with ISBN [%s] already exists.".formatted(isbn));

        verify(bookDAO, never()).addBook(any());
    }

    @Test
    void shouldThrowWhenUpdatingBookWithExistingIsbn() {
        // Given
        String newIsbn = "12043953321";
        Long id = 1L;

        Book existingBook = BookDTOMapper.INSTANCE.dtoToModel(
                getBookDTO(id, "1234567890123", "Lord of the Rings")
        );

        BookDTO request = getBookDTO(id, newIsbn, "Lord of the Rings");


        // --- book exists with that id
        given(bookDAO.findById(id)).willReturn(Optional.of(existingBook));


        // --- there is already a book with same isbn
        given(bookDAO.existsBookByIsbn(newIsbn)).willReturn(true);

        // When && Then
        assertThatThrownBy(() -> underTest.updateBook(id, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Book with ISBN [%s] already exists.".formatted(newIsbn));

        verify(bookDAO, never()).updateBook(any());
    }

    @Test
    void shouldThrowWhenUpdateHasNoChanges() {
        // Given
        Long id = 1L;
        String isbn = "12043953321";
        BookDTO request = getBookDTO(id, isbn, "Lord of the Rings");

        given(bookDAO.findById(id)).willReturn(Optional.of(BookDTOMapper.INSTANCE.dtoToModel(request)));

        // When && Then
        assertThatThrownBy(() -> underTest.updateBook(id, request))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("No data changes found.");

        verify(bookDAO, never()).updateBook(any());

    }

    BookDTO getBookDTO(Long id, String isbn, String title){

        AuthorDTO author = new AuthorDTO(1L, "Douglas", "Norman");
        List<AuthorDTO> authors = new ArrayList<>();
        authors.add(author);

        CategoryDTO category = new CategoryDTO(1L, "Mystery", "Mystery");
        List<CategoryDTO> categories = new ArrayList<>();
        categories.add(category);

        return new BookDTO(
                id,
                isbn,
                title,
                "The best book",
                BigDecimal.valueOf(19.99),
                300,
                250,
                LocalDate.of(1954, 7, 29),
                BookFormat.DIGITAL,
                authors,
                categories
        );

    }

}
