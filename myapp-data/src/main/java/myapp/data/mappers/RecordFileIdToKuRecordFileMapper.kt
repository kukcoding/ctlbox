package myapp.data.mappers

import myapp.data.entities.KuRecordFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordFileIdToKuRecordFileMapper @Inject constructor(
) : Mapper<String, KuRecordFile> {
    override suspend fun map(from: String) = KuRecordFile.createFromFileId(fileId = from)
}
