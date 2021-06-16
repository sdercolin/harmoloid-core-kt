package com.sdercolin.harmoloid.core.exception

import com.sdercolin.harmoloid.core.model.Note

class NoteOverlappingException(val trackIndex: Int, val notes: List<Note>) : Exception(
    "Overlapped notes found in track $trackIndex: $notes."
)
