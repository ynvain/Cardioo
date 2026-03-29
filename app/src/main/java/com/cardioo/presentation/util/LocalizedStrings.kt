package com.cardioo.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cardioo.R
import com.cardioo.domain.model.BpCategory
import com.cardioo.domain.model.Gender
import com.cardioo.domain.model.HeightUnit
import com.cardioo.domain.model.WeightUnit

@Composable
fun localizeBpCategory(category: BpCategory): String =
    when (category) {
        BpCategory.Hypotension -> stringResource(R.string.bp_category_hypotension)
        BpCategory.Normal -> stringResource(R.string.bp_category_normal)
        BpCategory.Elevated -> stringResource(R.string.bp_category_elevated)
        BpCategory.HypertensionStage1 -> stringResource(R.string.bp_category_hypertension_stage1)
        BpCategory.HypertensionStage2 -> stringResource(R.string.bp_category_hypertension_stage2)
        BpCategory.HypertensiveCrisis -> stringResource(R.string.bp_category_hypertensive_crisis)
    }

@Composable
fun localizeGender(gender: Gender): String =
    when (gender) {
        Gender.Male -> stringResource(R.string.gender_male)
        Gender.Female -> stringResource(R.string.gender_female)
    }

@Composable
fun weightUnitString(unit: WeightUnit): String =
    when (unit) {
        WeightUnit.KG -> stringResource(R.string.unit_kg)
        WeightUnit.LB -> stringResource(R.string.unit_lb)
    }

@Composable
fun heightUnitString(unit: HeightUnit): String =
    when (unit) {
        HeightUnit.CM -> stringResource(R.string.unit_cm)
        HeightUnit.IN -> stringResource(R.string.unit_in)
    }
