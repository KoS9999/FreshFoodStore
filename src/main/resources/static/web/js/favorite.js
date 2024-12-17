document.addEventListener('DOMContentLoaded', function () {
    console.log('DOM Loaded');

    // Xóa sản phẩm khỏi Wishlist
    const removeButtons = document.querySelectorAll('.remove-wishlist');
    removeButtons.forEach(button => {
        button.addEventListener('click', function (e) {
            e.preventDefault();
            const productId = this.getAttribute('data-product-id');
            console.log('Remove product from wishlist:', productId);

            fetch(`/wishlist/remove?productId=${productId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then(response => {
                    if (response.ok) {
                        console.log('Product removed from wishlist:', productId);
                        // Reload lại trang để cập nhật danh sách
                        location.reload();
                    } else {
                        throw new Error('Failed to remove product from wishlist');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Could not remove product from wishlist');
                });
        });
    });
});
document.addEventListener('DOMContentLoaded', function () {
    const buttons = document.querySelectorAll('.add-to-wishlist1');

    buttons.forEach(button => {
        button.addEventListener('click', function (e) {
            e.preventDefault();
            const productId = this.getAttribute('data-product-id');

            fetch(`/wishlist/add?productId=${productId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then(response => {
                    if (response.redirected) {
                        // Nếu server trả về redirect, chuyển hướng tới URL mới
                        window.location.href = response.url;
                        return;
                    }

                    if (!response.ok) {
                        throw new Error('Failed to add product to wishlist');
                    }

                    return response.text();
                })
                .then(data => {
                    console.log('Product added successfully:', data);
                })
                .catch(error => {
                    console.error('Error:', error); // Log lỗi
                });
        });
    });
});