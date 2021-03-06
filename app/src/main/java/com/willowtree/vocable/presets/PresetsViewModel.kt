package com.willowtree.vocable.presets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.willowtree.vocable.BaseViewModel
import com.willowtree.vocable.room.Category
import com.willowtree.vocable.room.Phrase
import kotlinx.coroutines.launch
import org.koin.core.inject

class PresetsViewModel : BaseViewModel() {

    private val presetsRepository: PresetsRepository by inject()

    private val liveCategoryList = MutableLiveData<List<Category>>()
    val categoryList: LiveData<List<Category>> = liveCategoryList

    private val liveSelectedCategory = MutableLiveData<Category>()
    val selectedCategory: LiveData<Category> = liveSelectedCategory

    private val liveCurrentPhrases = MutableLiveData<List<Phrase>>()
    val currentPhrases: LiveData<List<Phrase>> = liveCurrentPhrases

    init {
        populateCategories()
    }

    fun populateCategories() {
        backgroundScope.launch {
            val categories = presetsRepository.getAllCategories().filter { !it.hidden }
            liveCategoryList.postValue(categories)
            val currentCategory = liveSelectedCategory.value ?: categories.first()
            onCategorySelected(currentCategory)
        }
    }

    fun onCategorySelected(category: Category) {
        liveSelectedCategory.postValue(category)
        backgroundScope.launch {
            val cat = presetsRepository.getCategoryById(category.categoryId)

            // make sure the category wasn't deleted before getting its phrases
            if (cat != null) {
                val phrases = presetsRepository.getPhrasesForCategory(category.categoryId)
                    .sortedBy { it.sortOrder }
                liveCurrentPhrases.postValue(phrases)
            } else { // if the category has been deleted, select the first available category to show
                val categories = presetsRepository.getAllCategories().filter { !it.hidden }
                onCategorySelected(categories[0])
            }
        }
    }
}