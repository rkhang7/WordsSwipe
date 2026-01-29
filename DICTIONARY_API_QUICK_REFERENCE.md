# Dictionary API Integration - Quick Reference

## 🚀 Quick Start

### Inject the UseCase
```kotlin
@HiltViewModel
class WordViewModel @Inject constructor(
    private val getWordDetailUseCase: GetWordDetailUseCase
) : ViewModel()
```

### Call the Function
```kotlin
viewModelScope.launch {
    val wordDetail = getWordDetailUseCase("hello")
    _word.value = wordDetail
}
```

### Handle Errors
```kotlin
try {
    val wordDetail = getWordDetailUseCase("word")
} catch (e: WordNotFoundException) {
    // Word not found (404)
} catch (e: DictionaryApiException) {
    // API or network error
} catch (e: IllegalArgumentException) {
    // Invalid input (blank word)
}
```

---

## 📦 File Locations

| Component | File |
|-----------|------|
| API Interface | `data/remote/api/DictionaryApi.kt` |
| DTOs | `data/remote/model/WordDetailDto.kt` |
| Repository | `data/remote/repository/DictionaryRepository.kt` |
| Domain Models | `domain/model/WordDetail.kt` |
| UseCase | `domain/usecase/GetWordDetailUseCase.kt` |
| DI Module | `di/NetworkModule.kt` |

---

## 🔌 Dependency Injection

### Automatic
```kotlin
// No setup needed - automatically provided by Hilt
@Inject
private val useCase: GetWordDetailUseCase
```

### Manual (rarely needed)
```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.dictionaryapi.dev/api/v2/")
    .addConverterFactory(MoshiConverterFactory.create())
    .build()
val api = retrofit.create(DictionaryApi::class.java)
val repository = DictionaryRepository(api)
val useCase = GetWordDetailUseCase(repository)
```

---

## 📊 Data Access

### Word Information
```kotlin
val wordDetail = getWordDetailUseCase("hello")
wordDetail.word          // "hello"
wordDetail.phonetic      // "/həˈloʊ/"
wordDetail.phonetics     // List<Phonetic>
wordDetail.meanings      // List<Meaning>
```

### Phonetics
```kotlin
wordDetail.phonetics.forEach { phonetic ->
    phonetic.text       // Pronunciation text
    phonetic.audio      // Audio URL
}
```

### Meanings & Definitions
```kotlin
wordDetail.meanings.forEach { meaning ->
    meaning.partOfSpeech           // "verb", "noun", etc.
    meaning.definitions            // List<Definition>
    meaning.synonyms               // List<String>
    meaning.antonyms               // List<String>
    
    meaning.definitions.forEach { definition ->
        definition.definition      // Definition text
        definition.example         // Example usage
        definition.synonyms        // Synonyms for this def
    }
}
```

---

## ❌ Error Handling

| Exception | Cause | Handling |
|-----------|-------|----------|
| `WordNotFoundException` | 404 - Not found | Show "Word not found" |
| `DictionaryApiException` | 5xx or network | Show "API error" |
| `IllegalArgumentException` | Blank input | Show "Enter a word" |

---

## 🧪 Testing

### Mock Usage
```kotlin
whenever(mockRepository.getWordDetail("test"))
    .thenReturn(testWordDetail)

val result = useCase("test")
assertEquals("test", result.word)
```

### Tests Included
- ✓ Success responses
- ✓ 404 errors
- ✓ 5xx errors
- ✓ Network errors
- ✓ Empty responses
- ✓ Input validation
- ✓ DTO mapping
- ✓ Operator syntax

---

## 🎯 API Endpoint

```
GET https://api.dictionaryapi.dev/api/v2/entries/en/{word}
```

**Parameters**
- `word`: English word to look up

**Response**
- List of WordDetailResponse objects

---

## 📝 Common Patterns

### In ViewModel
```kotlin
private val _word = MutableStateFlow<WordDetail?>(null)
val word: StateFlow<WordDetail?> = _word.asStateFlow()

fun lookupWord(word: String) {
    viewModelScope.launch {
        try {
            _word.value = getWordDetailUseCase(word)
        } catch (e: Exception) {
            handleError(e)
        }
    }
}
```

### In Composable
```kotlin
val word by viewModel.word.collectAsStateWithLifecycle()

word?.let { detail ->
    Text(detail.word)
    Text(detail.phonetic ?: "")
    
    detail.meanings.forEach { meaning ->
        Text(meaning.partOfSpeech)
        meaning.definitions.forEach { def ->
            Text(def.definition)
        }
    }
}
```

### Error Handling
```kotlin
try {
    val detail = getWordDetailUseCase(searchText)
    showWord(detail)
} catch (e: WordNotFoundException) {
    showSnackbar("Word not found")
} catch (e: DictionaryApiException) {
    showSnackbar("Network error: ${e.message}")
}
```

---

## 🔒 Security Notes

✓ HTTPS only
✓ Input validation
✓ Error handling
✓ No data leakage

---

## ⚡ Performance Tips

- Use `collectAsStateWithLifecycle()` in Compose
- Implement caching with Room for offline support
- Use debouncing for search inputs
- Handle long responses gracefully

---

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| DICTIONARY_API_INTEGRATION.md | Complete guide |
| DICTIONARY_API_SUMMARY.md | Implementation overview |
| This file | Quick reference |

---

## ✅ Status

- Build: ✅ SUCCESS
- Tests: ✅ 13/13 PASSING
- Ready: ✅ PRODUCTION

