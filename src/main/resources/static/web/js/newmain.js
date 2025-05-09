//cart
$(document).on('click', '.product-add1', function(event) {
    event.preventDefault();
    const productId = $(this).data('product-id');

    const cartItems = JSON.parse(localStorage.getItem('cartItems')) || [];
    const existingItem = cartItems.find(item => item.id === productId);

    if (existingItem && existingItem.quantity >= 15) {
        return;
    }

    $.ajax({
        url: '/cart/add',
        type: 'GET',
        data: { productId: productId },
        success: function(response) {
            if (response.status === 'success') {
                console.log('Product added to cart:', response);

                // Lưu giỏ hàng vào localStorage
                saveCartToLocalStorage(response.cartItems, response.totalCartItems, response.totalPrice);

                // Cập nhật giao diện
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
function showErrorToast(message) {
    $('#errorToastMessage').html(message);
    const toast = new bootstrap.Toast(document.getElementById('errorToast'));
    toast.show();
}

$(document).on('click', '#addAllToCartBtn', function (event) {
    event.preventDefault();

    const productLinks = $('.related-tags a[data-product-id]');
    const cartItems = JSON.parse(localStorage.getItem('cartItems')) || [];

    let errorMessages = [];
    let totalProducts = productLinks.length;
    let completed = 0;

    productLinks.each(function () {
        const productId = parseInt($(this).data('product-id'));
        const productStatus = $(this).data('product-status');
        const productName = $(this).text();

        // Trường hợp hết hàng
        if (!productStatus || productStatus === false || productStatus === 'false') {
            errorMessages.push(`❌ <strong>${productName}</strong> đã hết hàng và không thể thêm vào giỏ.`);
            completed++;
            checkIfAllDone();
            return; // tiếp tục vòng lặp
        }

        // Trường hợp đã đủ 15
        const existingItem = cartItems.find(item => item.id === productId);
        if (existingItem && existingItem.quantity >= 15) {
            errorMessages.push(`⚠️ <strong>${productName}</strong> đã đạt giới hạn 15 sản phẩm.`);
            completed++;
            checkIfAllDone();
            return;
        }

        // Trường hợp còn hàng và chưa đủ 15
        $.ajax({
            url: '/cart/add',
            type: 'GET',
            data: { productId: productId },
            success: function (response) {
                if (response.status === 'success') {
                    saveCartToLocalStorage(response.cartItems, response.totalCartItems, response.totalPrice);
                    updateCartHeader(response.totalCartItems);
                    updateCartSidebar(response.cartItems, response.totalPrice);
                } else {
                    errorMessages.push(`🚫 ${response.message || 'Không thể thêm ' + productName}`);
                }
                completed++;
                checkIfAllDone();
            },
            error: function () {
                errorMessages.push(`🚫 Lỗi khi thêm <strong>${productName}</strong> vào giỏ hàng.`);
                completed++;
                checkIfAllDone();
            }
        });
    });

    function checkIfAllDone() {
        if (completed === totalProducts) {
            if (errorMessages.length > 0) {
                const message = errorMessages.join('<br>');
                showErrorToast(message);
            }
            openCartSidebar();
        }
    }
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
                document.getElementById('modal-product-img').src = productImg;
                document.getElementById('modal-product-name').innerText = productName;

                const wishlistModal = new bootstrap.Modal(document.getElementById('liton_wishlist_modal'));
                wishlistModal.show();
            })
            .catch(error => {
                console.error('Error adding to wishlist:', error);

                document.getElementById('modal-product-img').src = productImg;
                document.getElementById('modal-product-name').innerText = productName;

                const wishlistModal = new bootstrap.Modal(document.getElementById('liton_wishlist_modal'));
                wishlistModal.show();
            });
    }
});


// Hàm fetch sản phẩm mới và cập nhật DOM


// Hàm cập nhật số lượng sản phẩm trong header
function updateCartHeader(totalCartItems) {
    $('.mini-cart-icon sup').text(totalCartItems);
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
                            <input type="number" min="1" max="15" value="${item.quantity}"
                                   onchange="updateCartItemQuantity(${cartItemId}, this.value)" onkeydown="return false;"/>
                            x ${formatVND(item.unitPrice)}
                        </span>
                    </div>
                </li>
            `;
            cartMenu.append(itemHTML);
        });
    }

    $('#cartTotalPrice').text(`${formatVND(totalPrice)}`);
}


window.removeFromCart = function(productId) {
    $.ajax({
        url: '/cart/remove',
        type: 'POST',
        data: { productId: productId },
        success: function(response) {
            if (response.status === 'success') {
                console.log('Product removed:', response);
                saveCartToLocalStorage(response.cartItems, response.totalCartItems, response.totalPrice);
                updateCartHeader(response.totalCartItems);
                updateCartSidebar(response.cartItems, response.totalPrice);
            } else {
                alert(response.message || 'Error removing product.');
            }
        },
        error: function(error) {
            console.error('Error removing product:', error);
            alert('An error occurred. Please try again.');
        }
    });
};

window.updateCartItemQuantity = function(productId, newQuantity) {
    $.ajax({
        url: '/cart/update',
        type: 'POST',
        data: { productId: productId, quantity: newQuantity },
        success: function(response) {
            if (response.status === 'success') {
                console.log('Cart updated:', response);

                saveCartToLocalStorage(response.cartItems, response.totalCartItems, response.totalPrice);
                updateCartHeader(response.totalCartItems);
                updateCartSidebar(response.cartItems, response.totalPrice);
            } else {
                alert(response.message || 'Error updating cart.');
            }
        },
        error: function(error) {
            console.error('Error updating cart:', error);
            alert('An error occurred. Please try again.');
        }
    });
};

window.openCartSidebar = function() {
    $('#ltn__utilize-cart-menu').addClass('ltn__utilize-open');
};

document.getElementById('checkoutBtn').addEventListener('click', function(e) {
    e.preventDefault();

    const checkoutUrl = this.href;

    fetch('/cart/checkout/validate')
        .then(response => {
            if (!response.ok) {
                return response.text().then(errorMessage => {
                    showCartError(errorMessage);
                    window.openCartSidebar();
                });
            } else {
                window.location.href = checkoutUrl;
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showCartError('Đã xảy ra lỗi khi kiểm tra giỏ hàng');
            window.openCartSidebar();
        });
});

// Hàm hiển thị thông báo lỗi trong giỏ hàng
function showCartError(message) {
    const cartContent = document.querySelector('.mini-cart-product-area');

    // Xóa thông báo lỗi cũ nếu có
    const existingError = cartContent.querySelector('.checkout-error');
    if (existingError) existingError.remove();

    // Tạo thông báo lỗi mới
    const errorElement = document.createElement('div');
    errorElement.className = 'checkout-error alert alert-danger';
    errorElement.style.color = 'red';
    errorElement.style.padding = '10px';
    errorElement.style.margin = '10px 0';
    errorElement.style.borderRadius = '5px';
    errorElement.style.backgroundColor = '#ffe6e6';
    errorElement.style.border = '1px solid #ff0000';
    errorElement.textContent = message;

    // Thêm thông báo vào đầu phần giỏ hàng
    cartContent.prepend(errorElement);

    // Tự động ẩn thông báo sau 5 giây
    setTimeout(() => {
        errorElement.remove();
    }, 5000);
}

// Hàm xóa thông báo lỗi
function clearCartError() {
    const errorElement = document.querySelector('.checkout-error');
    if (errorElement) errorElement.remove();
}


// Hàm xóa toàn bộ giỏ hàng
window.clearCart = function() {
    localStorage.removeItem('cartItems');
    localStorage.removeItem('totalCartItems');
    updateCartHeader(0);
    updateCartSidebar([], 0);
};


// Gọi hàm khi DOM đã tải xong

function updateCartFromLocalStorage() {
    const cartItems = JSON.parse(localStorage.getItem('cartItems')) || [];
    const totalCartItems = localStorage.getItem('totalCartItems') || 0;
    const totalPrice = localStorage.getItem('totalPrice') || 0;

    updateCartHeader(totalCartItems);
    updateCartSidebar(cartItems, totalPrice);
}
function saveCartToLocalStorage(cartItems, totalCartItems, totalPrice) {
    localStorage.setItem('cartItems', JSON.stringify(cartItems));
    localStorage.setItem('totalCartItems', totalCartItems);
    localStorage.setItem('totalPrice', totalPrice);
}
document.addEventListener("DOMContentLoaded", function() {
    updateCartFromLocalStorage();
});


// Định dạng tiền VNĐ
function formatVND(amount) {

    if (amount === null || amount === undefined || amount === '') return '0 đ';

    const num = typeof amount === 'string' ?
        parseFloat(amount.replace(/[^0-9.-]+/g, "")) :
        amount;
    const roundedNum = Math.round(num);
    return new Intl.NumberFormat('vi-VN').format(roundedNum) + ' đ';
}
document.addEventListener('DOMContentLoaded', function() {
    // Format giá bán
    document.querySelectorAll('.js-price').forEach(el => {
        const price = el.getAttribute('data-price');
        el.textContent = formatVND(price);
    });
    document.querySelectorAll('.js-original-price').forEach(el => {
        const originalPrice = el.getAttribute('data-price');
        el.textContent = formatVND(originalPrice);
    });
    document.querySelectorAll('.js-format-vnd').forEach(el => {
        const price = el.getAttribute('data-price') || el.textContent;
        el.textContent = formatVND(price);
    });
})















