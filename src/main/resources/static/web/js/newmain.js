//cart
$(document).on('click', '.product-add1', function (event) {
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
        data: {productId: productId},
        success: function (response) {
            if (response.status === 'success') {
                console.log('Product added to cart:', response);

                saveCartToLocalStorage(response.cartItems, response.totalCartItems, response.totalPrice);

                updateCartHeader(response.totalCartItems);
                updateCartSidebar(response.cartItems, response.totalPrice);
                openCartSidebar();
            } else {
                alert(response.message || 'Error adding product to cart.');
            }
        },
        error: function (error) {
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
    const productIds = [];
    let errorMessages = [];

    productLinks.each(function () {
        const productId = parseInt($(this).data('product-id'));
        const productStatus = $(this).data('product-status');
        const productName = $(this).text();

        if (!productStatus || productStatus === false || productStatus === 'false') {
            errorMessages.push(`❌ <strong>${productName}</strong> đã hết hàng và không thể thêm vào giỏ.`);
            return;
        }

        const cartItems = JSON.parse(localStorage.getItem('cartItems')) || [];
        const existingItem = cartItems.find(item => item.id === productId);
        if (existingItem && existingItem.quantity >= 15) {
            errorMessages.push(`⚠️ <strong>${productName}</strong> đã đạt giới hạn 15 sản phẩm.`);
            return;
        }

        productIds.push(productId);
    });

    if (errorMessages.length > 0) {
        const message = errorMessages.join('<br>');
        showErrorToast(message);
    }

    if (productIds.length === 0 && errorMessages.length === 0) {
        showErrorToast('Không có sản phẩm nào để thêm vào giỏ hàng.');
        return;
    }

    if (productIds.length > 0) {
        $.ajax({
            url: '/cart/addMultiple',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ productIds: productIds }),
            success: function (response) {
                if (response.status === 'success') {
                    saveCartToLocalStorage(response.cartItems, response.totalCartItems, response.totalPrice);
                    updateCartHeader(response.totalCartItems);
                    updateCartSidebar(response.cartItems, response.totalPrice);
                    openCartSidebar();
                } else {
                    showErrorToast(response.message || 'Không thể thêm sản phẩm vào giỏ hàng.');
                }
            },
            error: function () {
                showErrorToast('Lỗi khi thêm sản phẩm vào giỏ hàng.');
            }
        });
    }
});

document.addEventListener('click', function (event) {
    if (event.target.closest('.add-to-wishlist1')) {
        event.preventDefault();

        const button = event.target.closest('.add-to-wishlist1');
        const productId = button.getAttribute('data-product-id');
        const productName = button.getAttribute('data-product-name');
        const productImg = button.getAttribute('src');

        console.log(`Add to wishlist clicked: Product ID: ${productId}, Name: ${productName}`);

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



function updateCartHeader(totalCartItems) {
    $('.mini-cart-icon sup').text(totalCartItems);
}

function updateCartSidebar(cartItems, totalPrice) {
    const cartMenu = $('.mini-cart-product-area .mini-cart-item');
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
                        <h6><a style="font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;" href="#">${item.name}</a></h6>
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


window.removeFromCart = function (productId) {
    $.ajax({
        url: '/cart/remove',
        type: 'POST',
        data: {productId: productId},
        success: function (response) {
            if (response.status === 'success') {
                console.log('Product removed:', response);
                saveCartToLocalStorage(response.cartItems, response.totalCartItems, response.totalPrice);
                updateCartHeader(response.totalCartItems);
                updateCartSidebar(response.cartItems, response.totalPrice);
            } else {
                alert(response.message || 'Error removing product.');
            }
        },
        error: function (error) {
            console.error('Error removing product:', error);
            alert('An error occurred. Please try again.');
        }
    });
};

window.updateCartItemQuantity = function (productId, newQuantity) {
    $.ajax({
        url: '/cart/update',
        type: 'POST',
        data: {productId: productId, quantity: newQuantity},
        success: function (response) {
            if (response.status === 'success') {
                console.log('Cart updated:', response);

                saveCartToLocalStorage(response.cartItems, response.totalCartItems, response.totalPrice);
                updateCartHeader(response.totalCartItems);
                updateCartSidebar(response.cartItems, response.totalPrice);
            } else {
                alert(response.message || 'Error updating cart.');
            }
        },
        error: function (error) {
            console.error('Error updating cart:', error);
            alert('An error occurred. Please try again.');
        }
    });
};

window.openCartSidebar = function () {
    $('#ltn__utilize-cart-menu').addClass('ltn__utilize-open');
};

document.getElementById('checkoutBtn').addEventListener('click', function (e) {
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

function showCartError(message) {
    const cartContent = document.querySelector('.mini-cart-product-area');

    const existingError = cartContent.querySelector('.checkout-error');
    if (existingError) existingError.remove();

    const errorElement = document.createElement('div');
    errorElement.className = 'checkout-error alert alert-danger';
    errorElement.style.color = 'red';
    errorElement.style.padding = '10px';
    errorElement.style.margin = '10px 0';
    errorElement.style.borderRadius = '5px';
    errorElement.style.backgroundColor = '#ffe6e6';
    errorElement.style.border = '1px solid #ff0000';
    errorElement.textContent = message;

    cartContent.prepend(errorElement);

    setTimeout(() => {
        errorElement.remove();
    }, 5000);
}

function clearCartError() {
    const errorElement = document.querySelector('.checkout-error');
    if (errorElement) errorElement.remove();
}


window.clearCart = function () {
    localStorage.removeItem('cartItems');
    localStorage.removeItem('totalCartItems');
    updateCartHeader(0);
    updateCartSidebar([], 0);
};



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

document.addEventListener("DOMContentLoaded", function () {
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

document.addEventListener('DOMContentLoaded', function () {
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

//Quick-view
document.addEventListener('DOMContentLoaded', function () {
    const quickViewButtons = document.querySelectorAll('.quick-view-btn');

    quickViewButtons.forEach(button => {
        button.addEventListener('click', function () {
            const productId = this.getAttribute('data-product-id');
            const productName = this.getAttribute('data-product-name');
            const productImage = this.getAttribute('data-product-image');
            const productPrice = parseFloat(this.getAttribute('data-product-price'));
            const productOriginalPrice = this.getAttribute('data-product-original-price');
            const productDiscount = parseFloat(this.getAttribute('data-product-discount') || 0);
            const productStatus = this.getAttribute('data-product-status') === 'true';
            const productRating = parseFloat(this.getAttribute('data-product-rating') || 0);
            const productCategory = this.getAttribute('data-product-category');

            console.log({
                productId,
                productName,
                productImage,
                productPrice,
                productOriginalPrice,
                productDiscount,
                productStatus,
                productRating,
                productCategory
            });

            document.getElementById('modal-product-image').src = productImage || '/uploads/main/default-image.jpg';
            document.getElementById('view-modal-product-name').textContent = productName || 'Unknown Product';

            const priceElement = document.getElementById('modal-product-price');
            priceElement.className = 'js-price';
            priceElement.setAttribute('data-price', productPrice || '');
            priceElement.textContent = productPrice !== null && !isNaN(productPrice) ? `${productPrice.toLocaleString('vi-VN')} đ` : 'N/A';

            const originalPriceElement = document.getElementById('modal-product-original-price');
            originalPriceElement.className = 'js-original-price';
            if (productDiscount > 0 && productOriginalPrice) {
                originalPriceElement.setAttribute('data-price', productOriginalPrice || '');
                originalPriceElement.textContent = !isNaN(parseFloat(productOriginalPrice)) ? `${parseFloat(productOriginalPrice).toLocaleString('vi-VN')} đ` : 'N/A';
                originalPriceElement.style.display = 'inline';
            } else {
                originalPriceElement.style.display = 'none';
            }

            const badgeContainer = document.getElementById('modal-product-badge').querySelector('ul');
            badgeContainer.innerHTML = '';
            if (!productStatus) {
                const li = document.createElement('li');
                li.style.backgroundColor = '#ff8585';
                li.innerHTML = '<span>Out of Stock</span>';
                badgeContainer.appendChild(li);
            } else if (productDiscount > 0) {
                const li = document.createElement('li');
                li.className = 'sale-badge';
                li.innerHTML = `<span>Discount: ${productDiscount}%</span>`;
                badgeContainer.appendChild(li);
            }

            const ratingContainer = document.getElementById('modal-product-ratting').querySelector('ul');
            ratingContainer.innerHTML = '';
            for (let i = 1; i <= 5; i++) {
                const li = document.createElement('li');
                const a = document.createElement('a');
                const icon = document.createElement('i');
                icon.className = i <= Math.floor(productRating) ? 'fas fa-star' :
                    (i - 0.5 <= productRating && productRating < i ? 'fas fa-star-half-alt' : 'far fa-star');
                a.appendChild(icon);
                li.appendChild(a);
                ratingContainer.appendChild(li);
            }

            const addToCartButton = document.getElementById('modal-add-to-cart');
            addToCartButton.setAttribute('data-product-id', productId || '');
            if (!productStatus) {
                addToCartButton.classList.add('disabled');
                addToCartButton.setAttribute('title', 'Out of Stock');
            } else {
                addToCartButton.classList.remove('disabled');
                addToCartButton.setAttribute('title', 'Add to Cart');
            }

            const addToWishlistButton = document.getElementById('modal-add-to-wishlist');
            addToWishlistButton.setAttribute('data-product-id', productId || '');
            if (!productStatus) {
                addToWishlistButton.classList.add('disabled');
                addToWishlistButton.setAttribute('title', 'Out of Stock');
            } else {
                addToWishlistButton.classList.remove('disabled');
                addToWishlistButton.setAttribute('title', 'Add to Wishlist');
            }

            document.getElementById('modal-product-categories').innerHTML = `<a href="#">${productCategory || 'Unknown Category'}</a>`;
        });
    });
});














