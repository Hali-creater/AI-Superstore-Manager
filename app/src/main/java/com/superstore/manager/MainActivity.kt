package com.superstore.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.superstore.manager.data.repository.DashboardRepository
import com.superstore.manager.data.repository.ProductRepository
import com.superstore.manager.data.repository.SalesRepository
import com.superstore.manager.data.repository.StockRepository
import com.superstore.manager.ui.bills.BillImportScreen
import com.superstore.manager.ui.bills.BillReviewScreen
import com.superstore.manager.ui.dashboard.DashboardViewModel
import com.superstore.manager.ui.dashboard.DashboardViewModelFactory
import com.superstore.manager.ui.dashboard.HomeScreen
import com.superstore.manager.ui.navigation.Screen
import com.superstore.manager.ui.navigation.bottomNavItems
import com.superstore.manager.ui.product.*
import com.superstore.manager.ui.reports.ReportsScreen
import com.superstore.manager.ui.sales.ManualSalesScreen
import com.superstore.manager.ui.sales.SalesViewModel
import com.superstore.manager.ui.sales.SalesViewModelFactory
import com.superstore.manager.ui.settings.SettingsScreen
import com.superstore.manager.ui.stock.LowStockScreen
import com.superstore.manager.ui.stock.StockReceivingScreen
import com.superstore.manager.ui.theme.SuperstoreTheme
import com.superstore.manager.util.LanguageHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as SuperstoreApp
        val db = app.database
        val dashboardRepo = DashboardRepository(db)
        val productRepo = ProductRepository(db)
        val salesRepo = SalesRepository(db)
        val stockRepo = StockRepository(db)

        setContent {
            var currentLanguage by remember { mutableStateOf(LanguageHelper.LANG_ENGLISH) }

            SuperstoreTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            bottomNavItems.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = null) },
                                    label = { Text(stringResource(screen.titleRes)) },
                                    selected = currentDestination?.route == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            val dashboardVm: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(dashboardRepo))
                            HomeScreen(
                                viewModel = dashboardVm,
                                onNavigateToSales = { navController.navigate(Screen.Sales.route) },
                                onNavigateToAddProduct = { navController.navigate(Screen.AddEditProduct.createRoute()) },
                                onNavigateToBills = { navController.navigate(Screen.Bills.route) },
                                onNavigateToLowStock = { navController.navigate(Screen.LowStockList.route) },
                                onNavigateToStockReceiving = { navController.navigate(Screen.StockReceiving.route) }
                            )
                        }
                        composable(Screen.Products.route) {
                            val productCatalogueVm: ProductCatalogueViewModel = viewModel(factory = ProductCatalogueViewModelFactory(productRepo))
                            ProductCatalogueScreen(
                                viewModel = productCatalogueVm,
                                onNavigateToAddProduct = { navController.navigate(Screen.AddEditProduct.createRoute()) },
                                onNavigateToEditProduct = { id -> navController.navigate(Screen.AddEditProduct.createRoute(id)) },
                                onNavigateToProductDetail = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) }
                            )
                        }
                        composable(
                            route = "product_add_edit?productId={productId}",
                            arguments = listOf(navArgument("productId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            })
                        ) { backStackEntry ->
                            val pIdStr = backStackEntry.arguments?.getString("productId")
                            val pId = pIdStr?.toLongOrNull()
                            val addEditVm: AddEditProductViewModel = viewModel(
                                key = "add_edit_$pIdStr",
                                factory = AddEditProductViewModelFactory(productRepo, pId)
                            )
                            AddEditProductScreen(
                                viewModel = addEditVm,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "product_detail/{productId}",
                            arguments = listOf(navArgument("productId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                            ProductDetailScreen(
                                productId = productId,
                                repository = productRepo,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToEdit = { id -> navController.navigate(Screen.AddEditProduct.createRoute(id)) }
                            )
                        }
                        composable(Screen.Sales.route) {
                            val salesVm: SalesViewModel = viewModel(factory = SalesViewModelFactory(productRepo, salesRepo))
                            ManualSalesScreen(
                                viewModel = salesVm
                            )
                        }
                        val billRepo = com.superstore.manager.data.repository.BillImportRepository(db)

                        composable(Screen.Bills.route) {
                            BillImportScreen(
                                repository = billRepo,
                                onNavigateToReview = { billId -> navController.navigate(Screen.BillReview.createRoute(billId)) }
                            )
                        }
                        composable(
                            route = "bill_review/{billId}",
                            arguments = listOf(navArgument("billId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val billId = backStackEntry.arguments?.getLong("billId") ?: 0L
                            BillReviewScreen(
                                billId = billId,
                                billRepository = billRepo,
                                productRepository = productRepo,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Reports.route) {
                            ReportsScreen(
                                reportsRepository = com.superstore.manager.data.repository.ReportsRepository(db),
                                productRepository = productRepo
                            )
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                currentLanguage = currentLanguage,
                                onLanguageSelected = { lang -> currentLanguage = lang }
                            )
                        }
                        composable(Screen.LowStockList.route) {
                            LowStockScreen(
                                repository = productRepo,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToProductDetail = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) }
                            )
                        }
                        composable(Screen.StockReceiving.route) {
                            StockReceivingScreen(
                                productRepository = productRepo,
                                stockRepository = stockRepo,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
