package com.sdercolin.harmoloid.core.exception

import com.sdercolin.harmoloid.core.model.Note

abstract class InvalidNoteException(val trackIndex: Int, val note: Note, detailMessage: String) : Exception(
    "Invalid note found in track $trackIndex: $note. $detailMessage"
) {
    class KeyOutOfRange(trackIndex: Int, note: Note, val rangeDescription: String) : InvalidNoteException(
        trackIndex, note, "Key is out of range ($rangeDescription)"
    )

    class InvalidLength(trackIndex: Int, note: Note) : InvalidNoteException(
        trackIndex, note, "Length of a note should be positive."
    )
}
