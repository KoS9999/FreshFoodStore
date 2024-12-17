//cart
// Sử dụng Event Delegation để xử lý sự kiện trên nội dung tải động
$(document).on('click', '.product-add1', function(event) {
    event.preventDefault();

    var productId = $(this).data('product-id');
    console.log('Add to cart clicked for Product ID:', productId);

    // AJAX call để thêm vào giỏ hàng
    $.ajax({
        url: '/cart/add',
        type: 'GET',
        data: { productId: productId },
        success: function(response) {
            if (response.status === 'success') {
                console.log('Product added to cart:', response);
                updateCartHeader(response.totalCartItems);
                updateCartSidebar(response.cartItems, response.totalPrice);
                openCartSidebar();
            } else {
                alert(response.message || 'Error adding product to cart.');
            }
        },
        error: function(error) {
            console.error('Error adding to cart:', error);
            alert('An error occurred. Please try again.');
        }
    });
});


//thêm favorite
document.addEventListener('click', function (event) {
    if (event.target.closest('.add-to-wishlist1')) {
        event.preventDefault();

        const button = event.target.closest('.add-to-wishlist1');
        const productId = button.getAttribute('data-product-id');
        const productName = button.getAttribute('data-product-name');
        const productImg = button.getAttribute('src');

        console.log(`Add to wishlist clicked: Product ID: ${productId}, Name: ${productName}`);

        // Gửi yêu cầu thêm sản phẩm vào Wishlist
        fetch(`/wishlist/add?productId=${productId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then(response => {
                if (response.redirected) {
                    window.location.href = response.url;
                    return;
                }
                if (!response.ok) {
                    throw new Error('Failed to add product to wishlist');
                }
                return response.text();
            })
            .then(data => {
                // Cập nhật modal với dữ liệu động
                document.getElementById('modal-product-img').src = productImg;
                document.getElementById('modal-product-name').innerText = productName;

                // Hiển thị modal
                const wishlistModal = new bootstrap.Modal(document.getElementById('liton_wishlist_modal'));
                wishlistModal.show();
            })
            .catch(error => {
                console.error('Error adding to wishlist:', error);

                // Hiển thị modal trong trường hợp lỗi
                document.getElementById('modal-product-img').src = productImg;
                document.getElementById('modal-product-name').innerText = productName;

                const wishlistModal = new bootstrap.Modal(document.getElementById('liton_wishlist_modal'));
                wishlistModal.show();
            });
    }
});


// Hàm fetch sản phẩm mới và cập nhật DOM
function fetchNewProducts() {
    fetch('/new-products')
        .then(response => response.text())
        .then(html => {
            // Cập nhật nội dung của new-products
            document.querySelector('#new-products').innerHTML = html;

            // Log để kiểm tra nội dung tải về
            console.log('Fetched and updated new products');
        })
        .catch(error => console.error('Error fetching new products:', error));
}

// Hàm cập nhật số lượng sản phẩm trong header
function updateCartHeader(totalCartItems) {
    $('.mini-cart-icon sup').text(totalCartItems);  // Cập nhật số lượng trong biểu tượng giỏ hàng
}

// Hàm cập nhật giỏ hàng trong sidebar
function updateCartSidebar(cartItems, totalPrice) {
    const cartMenu = $('.mini-cart-product-area .cart-list');
    cartMenu.empty(); // Xóa các sản phẩm cũ

    if (!Array.isArray(cartItems) || cartItems.length === 0) {
        cartMenu.html('<p>Hiện tại bạn chưa có sản phẩm nào trong giỏ hàng!</p>');
    } else {
        cartItems.forEach(item => {
            const productImage = item.productImage || 'default-image.png';
            const cartItemId = item.id || (item.product ? item.product.id : null);

            const itemHTML = `
                <li class="mini-cart-item clearfix">
                    <div class="mini-cart-img">
                        <a href="#"><img src="/uploads/main/${productImage}" alt="Product Image"></a>
                        <span class="mini-cart-item-delete" onclick="removeFromCart(${cartItemId})">
                            <i class="icon-cancel"></i>
                        </span>
                    </div>
                    <div class="mini-cart-info">
                        <h6><a href="#">${item.name}</a></h6>
                        <span class="mini-cart-quantity">
                            <input type="number" min="1" value="${item.quantity}"
                                   onchange="updateCartItemQuantity(${cartItemId}, this.value)" />
                            x $${item.unitPrice}
                        </span>
                    </div>
                </li>
            `;
            cartMenu.append(itemHTML);
        });
    }

    $('#cartTotalPrice').text(`$${totalPrice}`); // Hiển thị tổng giá tiền
}

// Hàm để xóa sản phẩm khỏi giỏ hàng
window.removeFromCart = function(productId) {
    $.ajax({
        url: '/cart/remove',
        type: 'POST',
        data: { productId: productId },
        success: function(response) {
            if (response.status === 'success') {
                localStorage.setItem('cartItems', JSON.stringify(response.cartItems));
                localStorage.setItem('totalCartItems', response.totalCartItems);
                updateCartHeader(response.totalCartItems);
                updateCartSidebar(response.cartItems, response.totalPrice);
            } else {
                alert(response.message);
            }
        },
        error: function(error) {
            alert('An error occurred while removing the product.');
        }
    });
};

// Hàm để cập nhật số lượng sản phẩm trong giỏ hàng
window.updateCartItemQuantity = function(productId, newQuantity) {
    $.ajax({
        url: '/cart/update',
        type: 'POST',
        data: { productId: productId, quantity: newQuantity },
        success: function(response) {
            if (response.status === 'success') {
                localStorage.setItem('cartItems', JSON.stringify(response.cartItems));
                localStorage.setItem('totalCartItems', response.totalCartItems);
                updateCartHeader(response.totalCartItems);
                updateCartSidebar(response.cartItems, response.totalPrice);
            } else {
                alert(response.message);
            }
        },
        error: function(error) {
            alert('An error occurred while updating the cart.');
        }
    });
};
window.openCartSidebar = function() {
    $('#ltn__utilize-cart-menu').addClass('ltn__utilize-open');
};

// Hàm xóa toàn bộ giỏ hàng
window.clearCart = function() {
    localStorage.removeItem('cartItems');
    localStorage.removeItem('totalCartItems');
    updateCartHeader(0);
    updateCartSidebar([], 0); // Cập nhật giỏ hàng trống
};

// Tải sản phẩm mới khi DOM đã sẵn sàng
document.addEventListener("DOMContentLoaded", function() {
    fetchNewProducts();
});

function fetchTopSellingProducts() {
    fetch('/top-selling-products')
        .then(response => response.text())
        .then(html => {
            document.querySelector('#top-selling-products').innerHTML = html;
            console.log('Fetched and updated top selling products');
        })
        .catch(error => console.error('Error fetching top selling products:', error));
}

// Gọi hàm khi DOM đã tải xong
document.addEventListener("DOMContentLoaded", function() {
    fetchTopSellingProducts();
});











//checkout
document.getElementById('createPaymentBtn').addEventListener('click', function () {
    const rows = document.querySelectorAll('#itemsTable tbody tr');
    const items = [];

    rows.forEach((row) => {
        const itemIdElem = row.querySelector('.itemid');
        const nameElem = row.querySelector('td:nth-child(2) span');
        const quantityElem = row.querySelector('td:nth-child(3) span');
        const unitPriceElem = row.querySelector('td:nth-child(4) span');

        if (itemIdElem && nameElem && quantityElem && unitPriceElem) {
            const item = {
                itemid: parseInt(itemIdElem.textContent.trim(), 10),
                itename: nameElem.textContent.trim(),
                itemprice: parseInt(unitPriceElem.textContent.trim(), 10),
                itemquantity: parseInt(quantityElem.textContent.trim(), 10),
            };
            items.push(item);
        }
    });

    document.getElementById('itemsInput').value = JSON.stringify(items);

    const embedData = {
        address: document.getElementById('address').value.trim(),
        phone: document.getElementById('phone').value.trim(),
        note: document.getElementById('note').value.trim(),
    };

    document.getElementById('embedDataInput').value = JSON.stringify(embedData);
    document.getElementById('zalopayForm').submit();
});



document.getElementById("zalopayForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const formData = new FormData(event.target);
    const requestData = Object.fromEntries(formData.entries());

    try {
        const response = await fetch(event.target.action, {
            method: "POST",
            body: new URLSearchParams(requestData),
        });

        if (response.ok) {
            const data = await response.json();
            if (data.order_url) {
                // Chuyển hướng sang trang thanh toán ZaloPay
                window.location.href = data.order_url;
            } else {
                console.error("Không nhận được order_url từ ZaloPay");
                alert("Không thể tạo giao dịch thanh toán. Vui lòng thử lại.");
            }
        } else {
            const error = await response.text();
            console.error("Lỗi:", error);
            alert("Có lỗi xảy ra: " + error);
        }
    } catch (error) {
        console.error("Lỗi khi kết nối đến máy chủ:", error);
        alert("Có lỗi xảy ra khi kết nối đến máy chủ.");
    }
});
document.getElementById('createCODBtn').addEventListener('click', function () {
    const rows = document.querySelectorAll('#itemsTable tbody tr');
    const items = [];

    rows.forEach((row) => {
        const itemIdElem = row.querySelector('.itemid');
        const nameElem = row.querySelector('td:nth-child(2) span');
        const quantityElem = row.querySelector('td:nth-child(3) span');
        const unitPriceElem = row.querySelector('td:nth-child(4) span');

        if (itemIdElem && nameElem && quantityElem && unitPriceElem) {
            const item = {
                itemid: parseInt(itemIdElem.textContent.trim(), 10),
                itename: nameElem.textContent.trim(),
                itemprice: parseInt(unitPriceElem.textContent.trim(), 10),
                itemquantity: parseInt(quantityElem.textContent.trim(), 10),
            };
            items.push(item);
        }
    });

    document.getElementById('itemsInputCOD').value = JSON.stringify(items);

    const embedData = {
        address: document.getElementById('address').value.trim(),
        phone: document.getElementById('phone').value.trim(),
        note: document.getElementById('note').value.trim(),
    };

    document.getElementById('embedDataInputCOD').value = JSON.stringify(embedData);
    document.getElementById('codForm').submit();
});
document.getElementById("codForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const formData = new FormData(event.target);
    const requestData = Object.fromEntries(formData.entries());

    try {
        const response = await fetch(event.target.action, {
            method: "POST",
            body: new URLSearchParams(requestData),
        });

        if (response.ok) {
            const message = await response.text();
            alert("Đơn hàng của bạn đã được tạo thành công. Cảm ơn bạn đã sử dụng dịch vụ!");
        } else {
            const error = await response.text();
            console.error("Lỗi:", error);
            alert("Có lỗi xảy ra: " + error);
        }
    } catch (error) {
        console.error("Lỗi khi kết nối đến máy chủ:", error);
        alert("Có lỗi xảy ra khi kết nối đến máy chủ.");
    }
});







