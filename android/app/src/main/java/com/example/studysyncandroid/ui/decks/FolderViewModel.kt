package com.example.studysyncandroid.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysyncandroid.data.local.entities.FolderWithDecks
import com.example.studysyncandroid.data.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class FolderViewModel @Inject constructor(
    private val folderRepository: FolderRepository
) : ViewModel() {

    open val foldersWithDecks: StateFlow<List<FolderWithDecks>> = folderRepository.getFoldersWithDecksStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isRefreshing = MutableStateFlow(false)
    open val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _isRefreshing.value = true
        viewModelScope.launch {
            folderRepository.refreshFolders()
            _isRefreshing.value = false
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            folderRepository.createFolder(name)
        }
    }

    fun deleteFolder(id: String) {
        viewModelScope.launch {
            folderRepository.deleteFolder(id)
        }
    }
}
