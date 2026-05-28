package com.example.hallisanthe

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.database.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HalliSantheApp()
        }
    }
}

data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val price: String = "",
    val imageUrl: String = "",
    val seller: String = "",
    val description: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalliSantheApp() {

    val context = LocalContext.current

    val database =
        FirebaseDatabase.getInstance()
            .reference
            .child("products")

    var isLoggedIn by rememberSaveable {
        mutableStateOf(false)
    }

    var userName by rememberSaveable {
        mutableStateOf("")
    }

    var productName by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("")
    }

    var price by remember {
        mutableStateOf("")
    }

    var seller by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val productList = remember {
        mutableStateListOf<Product>()
    }

    var selectedProduct by remember {
        mutableStateOf<Product?>(null)
    }

    val drawerState =
        rememberDrawerState(
            initialValue = DrawerValue.Closed
        )

    /*
    GALLERY
    */

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            selectedImageUri = uri
        }

    /*
    CAMERA
    */

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { bitmap: Bitmap? ->

            if (bitmap != null) {

                val path =
                    MediaStore.Images.Media.insertImage(
                        context.contentResolver,
                        bitmap,
                        "CapturedImage",
                        null
                    )

                selectedImageUri = Uri.parse(path)
            }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                cameraLauncher.launch(null)

            } else {

                Toast.makeText(
                    context,
                    "Camera Permission Denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    /*
    FETCH PRODUCTS
    */

    LaunchedEffect(Unit) {

        database.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                productList.clear()

                for (item in snapshot.children) {

                    val product =
                        item.getValue(Product::class.java)

                    if (product != null) {
                        productList.add(product)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /*
    LOGIN PAGE
    */

    if (!isLoggedIn) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFF3E0),
                            Color(0xFFFFCC80),
                            Color(0xFFFFB74D)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),

                shape = RoundedCornerShape(24.dp)
            ) {

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "🛍 Halli Santhe Digital",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD84315)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Empowering Village Artisans ❤️"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = userName,

                        onValueChange = {
                            userName = it
                        },

                        label = {
                            Text("Enter Username")
                        },

                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {

                            if (userName.isNotEmpty()) {

                                isLoggedIn = true

                            } else {

                                Toast.makeText(
                                    context,
                                    "Enter Username",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },

                        modifier = Modifier.fillMaxWidth(),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD84315)
                        )
                    ) {

                        Text("ENTER APP")
                    }
                }
            }
        }

        return
    }

    val filteredProducts =
        productList.filter {

            it.name.contains(
                searchQuery,
                ignoreCase = true
            )
        }

    /*
    DRAWER
    */

    ModalNavigationDrawer(
        drawerState = drawerState,

        drawerContent = {

            ModalDrawerSheet {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD84315)),

                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = userName.take(1).uppercase(),

                            color = Color.White,

                            fontSize = 34.sp,

                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userName,

                        fontWeight = FontWeight.Bold,

                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Village Artisan Buyer")

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = "Top Sellers",

                        fontWeight = FontWeight.Bold,

                        fontSize = 20.sp,

                        color = Color(0xFFD84315)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SellerCard(
                        "Basappa Crafts",
                        "9876543210"
                    )

                    SellerCard(
                        "Kavya Toys",
                        "9123456780"
                    )

                    SellerCard(
                        "Ramesh Arts",
                        "9988776655"
                    )
                }
            }
        }
    ) {

        Scaffold(

            topBar = {

                TopAppBar(

                    title = {
                        Text("Halli Santhe Digital")
                    },

                    navigationIcon = {

                        IconButton(
                            onClick = {
                            }
                        ) {

                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = null
                            )
                        }
                    }
                )
            }

        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFF3E0),
                                Color(0xFFFFE0B2)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {

                Text(
                    text = "Welcome $userName 👋",

                    fontSize = 20.sp,

                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                /*
                UPLOAD SECTION
                */

                Card(
                    shape = RoundedCornerShape(24.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "📤 Artisan Upload",

                            fontWeight = FontWeight.Bold,

                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = productName,

                            onValueChange = {
                                productName = it
                            },

                            label = {
                                Text("Product Name")
                            },

                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = category,

                            onValueChange = {
                                category = it
                            },

                            label = {
                                Text("Category")
                            },

                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = price,

                            onValueChange = {
                                price = it
                            },

                            label = {
                                Text("Price ₹")
                            },

                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),

                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = seller,

                            onValueChange = {
                                seller = it
                            },

                            label = {
                                Text("Seller Name")
                            },

                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = description,

                            onValueChange = {
                                description = it
                            },

                            label = {
                                Text("Description")
                            },

                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {

                                galleryLauncher.launch("image/*")
                            },

                            modifier = Modifier.fillMaxWidth(),

                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00897B)
                            )
                        ) {

                            Text("Upload From Gallery")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {

                                cameraPermissionLauncher.launch(
                                    Manifest.permission.CAMERA
                                )
                            },

                            modifier = Modifier.fillMaxWidth(),

                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6A1B9A)
                            )
                        ) {

                            Text("Capture From Camera")
                        }

                        selectedImageUri?.let {

                            Spacer(modifier = Modifier.height(16.dp))

                            AsyncImage(
                                model = it,

                                contentDescription = null,

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(20.dp)),

                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {

                                if (
                                    productName.isNotEmpty() &&
                                    category.isNotEmpty() &&
                                    price.isNotEmpty() &&
                                    seller.isNotEmpty()
                                ) {

                                    val id =
                                        database.push().key!!

                                    val product = Product(
                                        id = id,
                                        name = productName,
                                        category = category,
                                        price = price,
                                        imageUrl = selectedImageUri?.toString() ?: "",
                                        seller = seller,
                                        description = description
                                    )

                                    database.child(id)
                                        .setValue(product)
                                        .addOnSuccessListener {

                                            Toast.makeText(
                                                context,
                                                "Product Uploaded Successfully",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            productName = ""
                                            category = ""
                                            price = ""
                                            seller = ""
                                            description = ""
                                            selectedImageUri = null
                                        }

                                        .addOnFailureListener {

                                            Toast.makeText(
                                                context,
                                                "Upload Failed",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                } else {

                                    Toast.makeText(
                                        context,
                                        "Fill all details",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },

                            modifier = Modifier.fillMaxWidth(),

                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD84315)
                            )
                        ) {

                            Text("Upload Product")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                /*
                SEARCH
                */

                OutlinedTextField(
                    value = searchQuery,

                    onValueChange = {
                        searchQuery = it
                    },

                    modifier = Modifier.fillMaxWidth(),

                    label = {
                        Text("Search Products")
                    },

                    leadingIcon = {

                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                /*
                EMPTY STATE
                */

                if (filteredProducts.isEmpty()) {

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(24.dp),

                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "😔 No Products Uploaded",

                                fontWeight = FontWeight.Bold,

                                fontSize = 22.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Local artisan products will appear here."
                            )
                        }
                    }

                } else {

                    /*
                    PRODUCT GRID
                    */

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),

                        modifier = Modifier.height(1000.dp),

                        horizontalArrangement = Arrangement.spacedBy(12.dp),

                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(filteredProducts) { product ->

                            ProductCard(
                                product = product,

                                onClick = {
                                    selectedProduct = product
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    /*
    PRODUCT DETAILS
    */

    selectedProduct?.let { product ->

        AlertDialog(

            onDismissRequest = {
                selectedProduct = null
            },

            confirmButton = {

                Button(
                    onClick = {

                        Toast.makeText(
                            context,
                            "Seller Contacted Successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        selectedProduct = null
                    }
                ) {

                    Text("Check Stock")
                }
            },

            title = {
                Text(product.name)
            },

            text = {

                Column {

                    AsyncImage(
                        model = product.imageUrl,

                        contentDescription = null,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp)),

                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Category: ${product.category}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "₹ ${product.price}",

                        fontWeight = FontWeight.Bold,

                        fontSize = 22.sp,

                        color = Color(0xFFD84315)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(product.description)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Seller: ${product.seller}")
                }
            }
        )
    }
}

@Composable
fun SellerCard(
    name: String,
    phone: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFE0B2)
        )
    ) {

        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            Text(
                text = name,

                fontWeight = FontWeight.Bold,

                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("📞 $phone")
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(20.dp)
    ) {

        Column {

            AsyncImage(
                model = product.imageUrl,

                contentDescription = null,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),

                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(12.dp)
            ) {

                Text(
                    text = product.name,

                    fontWeight = FontWeight.Bold,

                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(product.category)

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "₹ ${product.price}",

                    color = Color(0xFFD84315),

                    fontWeight = FontWeight.Bold,

                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        onClick()
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("View Details")
                }
            }
        }
    }
}