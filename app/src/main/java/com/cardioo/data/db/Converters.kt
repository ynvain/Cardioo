package com.cardioo.data.db

import androidx.room.TypeConverter
import com.cardioo.domain.model.Gender
import com.cardioo.domain.model.HeightUnit
import com.cardioo.domain.model.WeightUnit

class Converters {
    @TypeConverter fun weightUnitToString(v: WeightUnit?): String? = v?.name
    @TypeConverter fun stringToWeightUnit(v: String?): WeightUnit? = v?.let { WeightUnit.valueOf(it) }

    @TypeConverter fun heightUnitToString(v: HeightUnit?): String? = v?.name
    @TypeConverter fun stringToHeightUnit(v: String?): HeightUnit? = v?.let { HeightUnit.valueOf(it) }

    @TypeConverter fun genderToString(v: Gender?): String? = v?.name
    @TypeConverter fun stringToGender(v: String?): Gender? = v?.let { Gender.valueOf(it) }
}

