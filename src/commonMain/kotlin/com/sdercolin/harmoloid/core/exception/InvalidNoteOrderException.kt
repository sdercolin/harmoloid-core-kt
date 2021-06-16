package com.sdercolin.harmoloid.core.exception

import com.sdercolin.harmoloid.core.model.Note

class InvalidNoteOrderException(val trackIndex: Int, val notes: List<Note>) : Exception(
    "Invalid note order (by tickOn) found in track $trackIndex: $notes"
)
